package net.rajaonson.room_management_system.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

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

    public void sendConfirmationEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Confirmez votre demande de création de compte");
        message.setText("""
                Bonjour,

                Une demande de création de compte a été déposée avec cette adresse mail.
                Cliquez sur le lien ci-dessous pour confirmer votre demande :

                %s?token=%s

                """.formatted(validationUrl, token));

        mailSender.send(message);
    }
}
