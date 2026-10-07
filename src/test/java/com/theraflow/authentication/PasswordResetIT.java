package com.theraflow.authentication;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.theraflow.TestcontainersConfiguration;
import com.theraflow.account.AccountRepository;
import com.theraflow.account.model.Account;
import com.theraflow.account.model.AccountType;
import com.theraflow.authentication.dto.PasswordResetRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public class PasswordResetIT {
    private static final String PASSWORD_RESET_PATH = "/api/v1/auth/password-reset";
    private static final String ACCOUNT_EMAIL = "test@example.com";

    @Autowired
    RestTestClient restTestClient;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @AfterEach
    void cleanDatabase() {
        passwordResetTokenRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @RegisterExtension
    private static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("spring", "root"));

    @DisplayName("""
            requestPasswordReset
            WHEN provided Email is valid
            SHOULD create a reset token
            SAVE token hash into DB
            SENT reset link to provided email
            RETURN HttpStatus 200
            """)
    @Test
    void requestPasswordReset_success() throws MessagingException {
        saveTestAccount();

        requestPasswordReset(ACCOUNT_EMAIL)
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("If the email is registered, you'll get a reset link");

        assertThat(passwordResetTokenRepository.count())
                .as("A reset token should be created for the account")
                .isEqualTo(1);

        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();
        assertThat(greenMail.getReceivedMessages()).hasSize(1);

        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];

        assertThat(receivedMessage.getSubject()).isEqualTo("Password Reset");
        assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo(ACCOUNT_EMAIL);
        assertThat(receivedMessage.getFrom()[0].toString()).isEqualTo("no-reply@theraflow.com");
    }

    @DisplayName("""
            requestPasswordReset
            WHEN provided Email is not registered
            SHOULD NOT create a reset token
            SHOULD NOT send any email
            RETURN HttpStatus 200
            """)
    @Test
    void requestPasswordReset_doNothing() {
        saveTestAccount();
        String nonExistingEmail = "bad@example.com";

        requestPasswordReset(nonExistingEmail)
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("If the email is registered, you'll get a reset link");

        assertThat(passwordResetTokenRepository.count())
                .as("No tokens should be created for an invalid email")
                .isZero();

        assertThat(greenMail.getReceivedMessages())
                .as("No reset email should be dispatched")
                .isEmpty();
    }

    private RestTestClient.ResponseSpec requestPasswordReset(String email) {
        PasswordResetRequest request = new PasswordResetRequest(email);
        return restTestClient.post()
                .uri(PASSWORD_RESET_PATH)
                .body(request)
                .exchange();
    }

    private void saveTestAccount() {
        Account account = Account.builder()
                .passwordHash("password-hash")
                .email(ACCOUNT_EMAIL)
                .type(AccountType.THERAPIST)
                .build();

        accountRepository.save(account);
    }
}
