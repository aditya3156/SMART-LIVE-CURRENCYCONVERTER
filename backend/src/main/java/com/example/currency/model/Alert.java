package com.example.currency.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "target_currency", nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false, length = 2)
    private String operator; // <, >, <=, >=

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal threshold;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "last_triggered")
    private LocalDateTime lastTriggered;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean shouldTrigger(BigDecimal currentRate) {
        if (!enabled || currentRate == null) {
            return false;
        }

        return switch (operator) {
            case "<" -> currentRate.compareTo(threshold) < 0;
            case ">" -> currentRate.compareTo(threshold) > 0;
            case "<=" -> currentRate.compareTo(threshold) <= 0;
            case ">=" -> currentRate.compareTo(threshold) >= 0;
            default -> false;
        };
    }
}