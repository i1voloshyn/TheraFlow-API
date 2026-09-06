package com.theraflow.event;

import com.theraflow.exception.EmailDeliveryException;
import com.theraflow.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VerificationEmailListenerTest {
    private static final UUID ACCOUNT_ID =
            UUID.fromString("cc837471-3c4b-4d77-a825-c4c1cf3a1dc5");
    private static final String EMAIL = "therapist@example.com";
    private static final String TOKEN = "email-verification-token";

    @Mock
    private EmailService emailService;
    @InjectMocks
    private VerificationEmailListener listener;

    @Test
    void sendVerificationEmail_withValidEvent_sendsEmail() {
        VerificationEmailRequested event = verificationEmailRequested();

        listener.sendVerificationEmail(event);

        verify(emailService).sendVerificationEmail(EMAIL, TOKEN);
    }

    @Test
    void sendVerificationEmail_whenDeliveryFails_doesNotPropagateException() {
        VerificationEmailRequested event = verificationEmailRequested();
        EmailDeliveryException deliveryException = new EmailDeliveryException(
                EMAIL,
                new RuntimeException("SMTP unavailable")
        );
        doThrow(deliveryException)
                .when(emailService)
                .sendVerificationEmail(EMAIL, TOKEN);

        assertThatCode(() -> listener.sendVerificationEmail(event))
                .doesNotThrowAnyException();

        verify(emailService).sendVerificationEmail(EMAIL, TOKEN);
    }

    private VerificationEmailRequested verificationEmailRequested() {
        return new VerificationEmailRequested(ACCOUNT_ID, EMAIL, TOKEN);
    }
}
