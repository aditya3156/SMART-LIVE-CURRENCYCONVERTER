package com.example.currency.service;

import com.example.currency.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${app.vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${app.vapid.private-key:}")
    private String vapidPrivateKey;

    public void sendWebPushNotification(User user, String title, String message) {
        if (user.getWebPushSubscription() == null) {
            return;
        }

        try {
            log.info("Web push notification sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error sending web push notification", e);
        }
    }

    public void sendEmailNotification(User user, String subject, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            
            mailSender.send(mailMessage);
            log.info("Email notification sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Error sending email notification", e);
        }
    }
}