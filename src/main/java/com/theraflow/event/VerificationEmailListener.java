package com.theraflow.event;

import com.theraflow.exception.EmailDeliveryException;
import com.theraflow.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class VerificationEmailListener {
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendVerificationEmail(VerificationEmailRequested event) {
        try {
            emailService.sendVerificationEmail(event.email(), event.token());
        } catch (EmailDeliveryException ex) {
            log.error(
                    "Verification email delivery failed for account {}",
                    event.accountId(),
                    ex
            );
        }
    }
}
