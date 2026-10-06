package com.theraflow.event;

import com.theraflow.email.EmailService;
import com.theraflow.exception.EmailDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class PasswordResetEmailListener {
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendPasswordResetEmail(PasswordResetRequest event) {
        try {
            emailService.sendPasswordResetEmail(event.email(), event.token());
        } catch (EmailDeliveryException ex) {
            log.error(
                    "Password reset email delivery failed for email {}",
                    event.email(),
                    ex
            );
        }
    }
}
