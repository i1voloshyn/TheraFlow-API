package com.theraflow.account;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.theraflow.TestcontainersConfiguration;
import com.theraflow.account.dto.AccountRequest;
import com.theraflow.account.dto.AccountResponse;
import com.theraflow.account.dto.SignUpResponse;
import com.theraflow.account.model.AccountType;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.exception.model.ErrorResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public class SignUpIT {
    private static final String ACCOUNTS_PATH = "/api/v1/accounts";
    private static final String VALID_PASSWORD = "123!ValidPassword";

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private AccountRepository accountRepository;

    @RegisterExtension
    private static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("spring", "root"));

    @AfterEach
    void cleanDatabase() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Register valid request should persist account and send verification email")
    void signUp_success() throws MessagingException {
        String email = "test@email.com";
        AccountRequest request = new AccountRequest(email, VALID_PASSWORD, AccountType.THERAPIST);

        AccountResponse actual = signUp(request)
                .expectStatus().isCreated()
                .returnResult(AccountResponse.class).getResponseBody();

        assertThat(actual).isNotNull();
        assertThat(actual.id()).isNotNull();
        assertThat(actual.email()).isEqualTo(request.email());

        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();
        assertThat(greenMail.getReceivedMessages()).hasSize(1);

        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];

        assertThat(receivedMessage.getSubject()).isEqualTo("Account Verification");
        assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo(email);
        assertThat(receivedMessage.getFrom()[0].toString()).isEqualTo("no-reply@theraflow.com");
    }

    @Test
    @DisplayName("Register duplicate email should return conflict without sending another email")
    void signUp_duplicateEmail_returnsConflictAndDoesNotSendSecondEmail() {
        AccountRequest request = new AccountRequest(
                "duplicate@email.com",
                VALID_PASSWORD,
                AccountType.THERAPIST
        );

        signUp(request)
                .expectStatus().isCreated();
        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

        signUp(request)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(error -> {
                    assertThat(error.statusCode()).isEqualTo(HttpStatus.CONFLICT.value());
                    assertThat(error.errorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS  );
                });

        assertThat(accountRepository.count()).isOne();
        assertThat(greenMail.getReceivedMessages()).hasSize(1);
    }

    private RestTestClient.ResponseSpec signUp(AccountRequest request) {
        return restTestClient.post()
                .uri(ACCOUNTS_PATH)
                .body(request)
                .exchange();
    }
}
