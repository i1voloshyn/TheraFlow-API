package com.theraflow.account;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.theraflow.TestcontainersConfiguration;
import com.theraflow.account.dto.AccountRequest;
import com.theraflow.account.dto.AccountResponse;
import com.theraflow.account.dto.SignUpResponse;
import com.theraflow.account.model.Account;
import com.theraflow.account.model.AccountType;
import com.theraflow.application.JwtEmailVerificationTokenService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// Maybe for this test it will be more reasonable to create integration test Controller->Service->DataLayer

@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AccountServiceIT {
    private static final String EMAIL = "therapist@example.com";
    private static final String RAW_PASSWORD = "StrongPassword1!";

    @RegisterExtension
    private static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("spring", "root"));

    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signUpAndReturnsAccessToken() throws MessagingException {
        SignUpResponse response = accountService.signUp(
                new AccountRequest(EMAIL, RAW_PASSWORD, AccountType.THERAPIST)
        );

        Account savedAccount = accountRepository.findAccountByEmail(EMAIL).orElseThrow();
        //Email
        assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();
        assertThat(greenMail.getReceivedMessages()).hasSize(1);

        MimeMessage receivedMessage = greenMail.getReceivedMessages()[0];

        assertThat(receivedMessage.getSubject()).isEqualTo("Account Verification");
        assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo(EMAIL);
        assertThat(receivedMessage.getFrom()[0].toString()).isEqualTo("no-reply@theraflow.com");

        //Account
        assertThat(savedAccount.getId()).isEqualTo(response.account().id());
        assertThat(savedAccount.getEmail()).isEqualTo(EMAIL);
        assertThat(savedAccount.getType()).isEqualTo(AccountType.THERAPIST);
        assertThat(savedAccount.getEmailVerified()).isFalse();
        assertThat(passwordEncoder.matches(RAW_PASSWORD, savedAccount.getPasswordHash())).isTrue();
    }
}
