
package com.fundoonotes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String baseUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.base-url}") String baseUrl
    ) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
    }

    public void sendVerificationEmail(String email, String token) {

        String link = baseUrl
                + "/api/users/verify-email?token="
                + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("no-reply@fundoonotes.local");
        message.setTo(email);
        message.setSubject("Verify your Fundoo Notes account");

        message.setText(
                "Please verify your account using this link:\n"
                        + link
                        + "\n\nThis link expires after 24 hours."
        );

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String email, String token) {

        // This backend API accepts the token in a POST request.
        // A frontend reset form can later consume the link.
        String link = baseUrl
                + "/reset-password?token="
                + token;

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("no-reply@fundoonotes.local");
        message.setTo(email);
        message.setSubject("Reset your Fundoo Notes password");

        message.setText(
                "A password reset was requested.\n\n"
                        + "Reset link: " + link
                        + "\n\nThis link expires after 15 minutes."
                        + "\nIf you did not request this, ignore this email."
        );

        mailSender.send(message);
    }
}
