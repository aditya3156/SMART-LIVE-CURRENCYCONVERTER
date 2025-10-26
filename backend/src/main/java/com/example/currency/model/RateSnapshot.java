package com.example.currency.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateSnapshot implements Serializable {
    
    private String base;
    private Map<String, BigDecimal> rates;
    private LocalDateTime timestamp;
    private String source;
}