package com.x5.recruitment.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for sending notifications.
 * For MVP, this is a stub that logs emails instead of sending.
 * In production, configure proper mail server.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Send email notification.
     * Currently logs the email for MVP demonstration.
     */
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {}", to);
        log.info("Subject: {}", subject);
        log.info("Body: {}", body);
        
        // For MVP, we just log. In production, uncomment below:
        /*
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@x5.ru");
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw e;
        }
        */
    }
}
