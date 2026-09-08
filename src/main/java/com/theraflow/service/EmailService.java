package com.theraflow.service;

import com.theraflow.exception.EmailDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class EmailService {
    private static final String ACCOUNT_VERIFICATION = "Account Verification";
    private static final String VERIFY_EMAIL_PATH = "/api/v1/accounts/verify-email";
    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.mail-from}")
    private String fromEmail;
    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(ACCOUNT_VERIFICATION);
            helper.setText(createVerificationEmail(token), true);

            mailSender.send(message);

        } catch (MailException | MessagingException e) {
            throw new EmailDeliveryException(toEmail, e);
        }
    }

    private String createVerificationEmail(String token) {
        String confirmationUrl = generateVerificationUrl(token);

        return """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f7; color: #51545e; margin: 0; padding: 0;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #ffffff; padding: 35px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);">
                            <h2 style="color: #333333; margin-top: 0;">Welcome to our app!</h2>
                            <p style="font-size: 16px; line-height: 1.5; color: #51545e;">
                                Please confirm your email address to complete your registration and activate your account.
                            </p>
                            <div style="padding: 20px 0; text-align: center;">
                                <a href="%s" style="background-color: #007bff; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; font-size: 16px;">Confirm Email Address</a>
                            </div>
                            <p style="font-size: 14px; color: #6b7280; line-height: 1.4;">
                                If the button above doesn't work, copy and paste this link into your web browser:
                            </p>
                            <p style="font-size: 12px; color: #007bff; word-break: break-all;">
                                <a href="%s" style="color: #007bff;">%s</a>
                            </p>
                            <hr style="border: none; border-top: 1px solid #eaeaec; margin: 25px 0;">
                            <p style="font-size: 12px; color: #8f92a1; margin-bottom: 0;">
                                If you did not create an account, no further action is required.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(confirmationUrl, confirmationUrl, confirmationUrl);
    }

    private String generateVerificationUrl(String token) {
        return UriComponentsBuilder.fromPath(baseUrl)
                .path(VERIFY_EMAIL_PATH)
                .queryParam("token", token)
                .toUriString();
    }
}
