package com.example.currency.service;

import com.example.currency.model.Alert;
import com.example.currency.model.RateSnapshot;
import com.example.currency.model.User;
import com.example.currency.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Transactional
    public Alert createAlert(Alert alert) {
        return alertRepository.save(alert);
    }

    public List<Alert> getUserAlerts(User user) {
        return alertRepository.findByUserAndEnabledTrue(user);
    }

    @Transactional
    public void deleteAlert(Long alertId, User user) {
        alertRepository.deleteByIdAndUser(alertId, user);
    }

    @Transactional
    public Alert toggleAlert(Long alertId, User user) {
        Alert alert = alertRepository.findByIdAndUser(alertId, user)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        alert.setEnabled(!alert.getEnabled());
        return alertRepository.save(alert);
    }

    public void evaluateAlerts(RateSnapshot snapshot) {
        List<Alert> alerts = alertRepository.findByEnabledTrue();
        
        for (Alert alert : alerts) {
            try {
                String pair = alert.getBaseCurrency() + "/" + alert.getTargetCurrency();
                
                if (!snapshot.getBase().equals(alert.getBaseCurrency())) {
                    continue;
                }

                BigDecimal currentRate = snapshot.getRates().get(alert.getTargetCurrency());
                if (currentRate == null) {
                    continue;
                }

                if (alert.shouldTrigger(currentRate)) {
                    triggerAlert(alert, currentRate);
                }
            } catch (Exception e) {
                log.error("Error evaluating alert {}", alert.getId(), e);
            }
        }
    }

    @Transactional
    protected void triggerAlert(Alert alert, BigDecimal currentRate) {
        // Check if already triggered recently (within last hour)
        if (alert.getLastTriggered() != null &&
            alert.getLastTriggered().isAfter(LocalDateTime.now().minusHours(1))) {
            return;
        }

        log.info("Triggering alert {} for user {}", alert.getId(), alert.getUser().getEmail());

        // Update last triggered
        alert.setLastTriggered(LocalDateTime.now());
        alertRepository.save(alert);

        // Create notification message
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", "ALERT_TRIGGERED");
        notification.put("alertId", alert.getId());
        notification.put("pair", alert.getBaseCurrency() + "/" + alert.getTargetCurrency());
        notification.put("currentRate", currentRate);
        notification.put("threshold", alert.getThreshold());
        notification.put("operator", alert.getOperator());
        notification.put("message", String.format(
            "Alert: 1 %s %s %.4f %s (Current: %.4f)",
            alert.getBaseCurrency(),
            alert.getOperator(),
            alert.getThreshold(),
            alert.getTargetCurrency(),
            currentRate
        ));
        notification.put("timestamp", LocalDateTime.now());

        // Send WebSocket notification
        messagingTemplate.convertAndSendToUser(
            alert.getUser().getEmail(),
            "/queue/notifications",
            notification
        );

        // Send Web Push notification
        notificationService.sendWebPushNotification(
            alert.getUser(),
            "Currency Alert",
            notification.get("message").toString()
        );

        // Send email notification (if configured)
        notificationService.sendEmailNotification(
            alert.getUser(),
            "Currency Alert Triggered",
            notification.get("message").toString()
        );
    }
}