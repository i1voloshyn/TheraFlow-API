package com.theraflow.controller;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.theraflow.TestcontainersConfiguration;
import com.theraflow.dto.AccountRequest;
import com.theraflow.dto.AccountResponse;
import com.theraflow.exception.model.ErrorCode;
import com.theraflow.exception.model.ErrorResponse;
import com.theraflow.model.AccountType;
import com.theraflow.repository.AccountRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
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
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE="
)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public class AccountControllerIntegrationTest {
    private static final String ACCOUNTS_PATH = "/api/v1/accounts";
    private static final String VALID_PASSWORD = "123!ValidPassword";

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private AccountRepository accountRepository;

    @RegisterExtension
    private static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("spring", "root"));

    @BeforeEach
    void cleanDatabase() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("Register valid request should persist account and send verification email")
    void register_success() throws MessagingException {
        String email = "test@email.com";
        AccountRequest request = new AccountRequest(email, VALID_PASSWORD, AccountType.THERAPIST);

        AccountResponse response = register(request)
                .expectStatus().isCreated()
                .returnResult(AccountResponse.class).getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo(email);

        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();
        assertThat(greenMail.getReceivedMessages()).hasSize(1);

        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];

        assertThat(receivedMessage.getSubject()).isEqualTo("Account Verification");
        assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo(email);
        assertThat(receivedMessage.getFrom()[0].toString()).isEqualTo("no-reply@theraflow.com");
    }

    @Test
    @DisplayName("Register duplicate email should return conflict without sending another email")
    void register_duplicateEmail_returnsConflictAndDoesNotSendSecondEmail() {
        AccountRequest request = new AccountRequest(
                "duplicate@email.com",
                VALID_PASSWORD,
                AccountType.THERAPIST
        );

        register(request)
                .expectStatus().isCreated();
        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

        register(request)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ErrorResponse.class)
                .value(error -> {
                    assertThat(error.status()).isEqualTo(HttpStatus.CONFLICT.value());
                    assertThat(error.errorCode()).isEqualTo(ErrorCode.RESOURCE_CONFLICT);
                });

        assertThat(accountRepository.count()).isOne();
        assertThat(greenMail.getReceivedMessages()).hasSize(1);
    }

    private RestTestClient.ResponseSpec register(AccountRequest request) {
        return restTestClient.post()
                .uri(ACCOUNTS_PATH)
                .body(request)
                .exchange();
    }
}
