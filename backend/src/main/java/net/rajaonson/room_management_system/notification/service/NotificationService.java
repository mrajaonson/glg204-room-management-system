package net.rajaonson.room_management_system.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final String validationUrl;

    public NotificationService(JavaMailSender mailSender,
                               @Value("${app.mail.from}") String from,
                               @Value("${app.mail.validation-url}") String validationUrl) {
        this.mailSender = mailSender;
        this.from = from;
        this.validationUrl = validationUrl;
    }

    public boolean sendConfirmationEmail(String email, String token) {
        return send("confirmation", email,
                "Confirmez votre demande de création de compte",
                """
                Bonjour,

                Une demande de création de compte a été déposée avec cette adresse mail.
                Cliquez sur le lien ci-dessous pour confirmer votre demande :

                %s?token=%s

                """.formatted(validationUrl, token));
    }

    public boolean sendApprovalEmail(String email, String login) {
        return send("approval", email,
                "Votre demande de création de compte a été acceptée",
                """
                Bonjour,

                Votre demande de création de compte a été acceptée.
                Vous pouvez désormais vous connecter avec l'identifiant : %s

                """.formatted(login));
    }

    public boolean sendRefusalEmail(String email) {
        return send("refusal", email,
                "Votre demande de création de compte a été refusée",
                """
                Bonjour,

                Votre demande de création de compte a été refusée.

                """);
    }

    private boolean send(String kind, String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            log.info("sent {} email to {}", kind, to);
            return true;
        } catch (MailException e) {
            log.error("failed to send {} email to {}", kind, to, e);
            return false;
        }
    }
}
