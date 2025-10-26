package com.example.currency.service;

import com.example.currency.model.RateSnapshot;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RateService {

    private final WebClient.Builder webClientBuilder;
    private final SimpMessagingTemplate messagingTemplate;
    private final AlertService alertService;

    @Value("${app.exchange-api.key}")
    private String apiKey;

    @Value("${app.exchange-api.base-url}")
    private String apiBaseUrl;

    @Value("${app.mock-mode}")
    private boolean mockMode;

    // In-memory cache (simple alternative to Redis)
    private RateSnapshot cachedRates;
    private LocalDateTime lastFetch;

    @Scheduled(fixedDelayString = "${app.rate-fetch-interval}")
    public void fetchAndBroadcastRates() {
        try {
            RateSnapshot snapshot = fetchLatestRates("USD");
            
            // Broadcast via WebSocket
            messagingTemplate.convertAndSend("/topic/rates", snapshot);

            // Check alerts
            alertService.evaluateAlerts(snapshot);

            log.debug("Fetched and broadcasted rates: {} currencies", snapshot.getRates().size());
        } catch (Exception e) {
            log.error("Error fetching rates", e);
        }
    }

    public RateSnapshot fetchLatestRates(String base) {
        // Check in-memory cache first
        if (cachedRates != null && lastFetch != null && 
            lastFetch.plusMinutes(10).isAfter(LocalDateTime.now())) {
            log.debug("Returning cached rates for {}", base);
            return cachedRates;
        }

        if (mockMode) {
            RateSnapshot snapshot = generateMockRates(base);
            cachedRates = snapshot;
            lastFetch = LocalDateTime.now();
            return snapshot;
        }

        try {
            WebClient webClient = webClientBuilder.build();
            JsonNode response = webClient.get()
                    .uri(apiBaseUrl + "/latest/" + base)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("conversion_rates")) {
                Map<String, BigDecimal> rates = new HashMap<>();
                response.get("conversion_rates").fields().forEachRemaining(entry -> {
                    rates.put(entry.getKey(), new BigDecimal(entry.getValue().asText()));
                });

                RateSnapshot snapshot = RateSnapshot.builder()
                        .base(base)
                        .rates(rates)
                        .timestamp(LocalDateTime.now())
                        .source("ExchangeRatesAPI")
                        .build();

                // Cache the result in memory
                cachedRates = snapshot;
                lastFetch = LocalDateTime.now();

                log.info("Successfully fetched {} rates from API", rates.size());
                return snapshot;
            }
        } catch (Exception e) {
            log.error("Error fetching rates from API", e);
        }

        // Fallback to mock
        RateSnapshot snapshot = generateMockRates(base);
        cachedRates = snapshot;
        lastFetch = LocalDateTime.now();
        return snapshot;
    }

    public BigDecimal convert(String from, String to, BigDecimal amount) {
        RateSnapshot rates = fetchLatestRates(from);
        BigDecimal rate = rates.getRates().get(to);
        
        if (rate == null) {
            throw new IllegalArgumentException("Currency not supported: " + to);
        }

        return amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
    }

    public Map<String, Object> getHistoricalRates(String base, String target, String period) {
        return generateMockHistoricalData(base, target, period);
    }

    public Map<String, BigDecimal> predictShortTerm(String base, String target) {
        Map<String, Object> historical = getHistoricalRates(base, target, "1M");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) historical.get("data");
        
        // Simple moving average prediction
        double sum = 0;
        int count = Math.min(7, data.size());
        
        for (int i = data.size() - count; i < data.size(); i++) {
            sum += ((Number) data.get(i).get("rate")).doubleValue();
        }
        
        double avg = sum / count;
        BigDecimal prediction = BigDecimal.valueOf(avg).setScale(6, RoundingMode.HALF_UP);
        
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("current", (BigDecimal) data.get(data.size() - 1).get("rate"));
        result.put("predicted", prediction);
        result.put("confidence", BigDecimal.valueOf(0.75));
        
        return result;
    }

    private RateSnapshot generateMockRates(String base) {
        Map<String, BigDecimal> rates = new HashMap<>();
        Random random = new Random();
        
        if ("USD".equals(base)) {
            rates.put("INR", BigDecimal.valueOf(83.40 + random.nextDouble() * 0.5));
            rates.put("EUR", BigDecimal.valueOf(0.92 + random.nextDouble() * 0.01));
            rates.put("GBP", BigDecimal.valueOf(0.79 + random.nextDouble() * 0.01));
            rates.put("JPY", BigDecimal.valueOf(149.50 + random.nextDouble() * 2));
            rates.put("AUD", BigDecimal.valueOf(1.52 + random.nextDouble() * 0.02));
            rates.put("CAD", BigDecimal.valueOf(1.36 + random.nextDouble() * 0.02));
            rates.put("CHF", BigDecimal.valueOf(0.88 + random.nextDouble() * 0.01));
            rates.put("CNY", BigDecimal.valueOf(7.24 + random.nextDouble() * 0.1));
            rates.put("SGD", BigDecimal.valueOf(1.34 + random.nextDouble() * 0.02));
            rates.put("NZD", BigDecimal.valueOf(1.62 + random.nextDouble() * 0.02));
        }

        return RateSnapshot.builder()
                .base(base)
                .rates(rates)
                .timestamp(LocalDateTime.now())
                .source("Mock")
                .build();
    }

    private Map<String, Object> generateMockHistoricalData(String base, String target, String period) {
        List<Map<String, Object>> data = new ArrayList<>();
        int days = switch (period) {
            case "1W" -> 7;
            case "1M" -> 30;
            case "1Y" -> 365;
            default -> 30;
        };

        Random random = new Random();
        double baseRate = 83.40;
        
        for (int i = 0; i < days; i++) {
            double variation = (random.nextDouble() - 0.5) * 2;
            double rate = baseRate + variation;
            
            Map<String, Object> point = new HashMap<>();
            point.put("date", LocalDateTime.now().minusDays(days - i).toString());
            point.put("rate", BigDecimal.valueOf(rate).setScale(4, RoundingMode.HALF_UP));
            data.add(point);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("base", base);
        result.put("target", target);
        result.put("period", period);
        result.put("data", data);
        
        return result;
    }
}
