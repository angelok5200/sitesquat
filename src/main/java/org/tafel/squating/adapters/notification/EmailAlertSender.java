package org.tafel.squating.adapters.notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.tafel.squating.config.ApplicationProperties;
import org.tafel.squating.domain.model.Alert;
import org.tafel.squating.ports.outbound.AlertSender;

@Component
public class EmailAlertSender implements AlertSender {

    private final JavaMailSender mailSender;
    private final ApplicationProperties applicationProperties;

    public EmailAlertSender(
            JavaMailSender mailSender,
            ApplicationProperties applicationProperties
    ) {
        this.mailSender = mailSender;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public void send(Alert alert) {
        if (alert == null) {
            throw new IllegalArgumentException("alert must not be null");
        }

        String recipient = applicationProperties.getMailRecipient();

        if (recipient == null || recipient.isBlank()) {
            throw new IllegalStateException(
                    "Mail recipient must be configured"
            );
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(recipient);
        message.setSubject(
                "[SiteSquating] " + alert.getHeadline()
        );
        message.setText(buildMessage(alert));

        mailSender.send(message);
    }

    private String buildMessage(Alert alert) {
        return """
                SiteSquating alert

                Domain: %s
                Type: %s
                Severity: %s

                %s
                """.formatted(
                alert.getDomain(),
                alert.getType(),
                alert.getSeverity(),
                alert.getMessage()
        );
    }
}
