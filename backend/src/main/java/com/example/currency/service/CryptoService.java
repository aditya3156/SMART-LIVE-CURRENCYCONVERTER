package com.example.currency.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class CryptoService {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.crypto-api.base-url}")
    private String apiBaseUrl;

    @Value("${app.mock-mode}")
    private boolean mockMode;

    // In-memory cache
    private Map<String, BigDecimal> cachedPrices;
    private LocalDateTime lastFetch;

    public Map<String, BigDecimal> getCryptoPrices(String currency) {
        // Check in-memory cache (5 min)
        if (cachedPrices != null && lastFetch != null && 
            lastFetch.plusMinutes(5).isAfter(LocalDateTime.now())) {
            log.debug("Returning cached crypto prices for {}", currency);
            return cachedPrices;
        }

        if (mockMode) {
            Map<String, BigDecimal> prices = generateMockCryptoPrices(currency);
            cachedPrices = prices;
            lastFetch = LocalDateTime.now();
            return prices;
        }

        try {
            WebClient webClient = webClientBuilder.build();
            String currencyLower = currency.toLowerCase();
            String apiUrl = apiBaseUrl + "/simple/price?ids=bitcoin,ethereum,litecoin,binancecoin,ripple&vs_currencies=" + currencyLower;
            
            log.debug("Fetching crypto prices from: {}", apiUrl);
            
            JsonNode response = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            Map<String, BigDecimal> prices = new HashMap<>();
            if (response != null) {
                log.debug("Crypto API response: {}", response.toString());
                
                // Parse Bitcoin
                if (response.has("bitcoin") && response.get("bitcoin").has(currencyLower)) {
                    prices.put("BTC", BigDecimal.valueOf(response.get("bitcoin").get(currencyLower).asDouble()));
                }
                
                // Parse Ethereum
                if (response.has("ethereum") && response.get("ethereum").has(currencyLower)) {
                    prices.put("ETH", BigDecimal.valueOf(response.get("ethereum").get(currencyLower).asDouble()));
                }
                
                // Parse Litecoin
                if (response.has("litecoin") && response.get("litecoin").has(currencyLower)) {
                    prices.put("LTC", BigDecimal.valueOf(response.get("litecoin").get(currencyLower).asDouble()));
                }
                
                // Parse Binance Coin
                if (response.has("binancecoin") && response.get("binancecoin").has(currencyLower)) {
                    prices.put("BNB", BigDecimal.valueOf(response.get("binancecoin").get(currencyLower).asDouble()));
                }
                
                // Parse Ripple
                if (response.has("ripple") && response.get("ripple").has(currencyLower)) {
                    prices.put("XRP", BigDecimal.valueOf(response.get("ripple").get(currencyLower).asDouble()));
                }
                
                if (!prices.isEmpty()) {
                    cachedPrices = prices;
                    lastFetch = LocalDateTime.now();
                    log.info("Successfully fetched {} crypto prices from API", prices.size());
                    return prices;
                }
            }
        } catch (Exception e) {
            log.error("Error fetching crypto prices from API: {}", e.getMessage(), e);
        }

        log.warn("Falling back to mock crypto prices");
        Map<String, BigDecimal> prices = generateMockCryptoPrices(currency);
        cachedPrices = prices;
        lastFetch = LocalDateTime.now();
        return prices;
    }

    private Map<String, BigDecimal> generateMockCryptoPrices(String currency) {
        Map<String, BigDecimal> prices = new HashMap<>();
        Random random = new Random();
        
        if ("INR".equalsIgnoreCase(currency)) {
            prices.put("BTC", BigDecimal.valueOf(3589320 + random.nextInt(10000)));
            prices.put("ETH", BigDecimal.valueOf(195650 + random.nextInt(5000)));
            prices.put("LTC", BigDecimal.valueOf(6531 + random.nextInt(200)));
            prices.put("BNB", BigDecimal.valueOf(46800 + random.nextInt(1000)));
            prices.put("XRP", BigDecimal.valueOf(45 + random.nextInt(5)));
        } else {
            prices.put("BTC", BigDecimal.valueOf(43000 + random.nextInt(1000)));
            prices.put("ETH", BigDecimal.valueOf(2300 + random.nextInt(100)));
            prices.put("LTC", BigDecimal.valueOf(72 + random.nextInt(5)));
            prices.put("BNB", BigDecimal.valueOf(560 + random.nextInt(20)));
            prices.put("XRP", BigDecimal.valueOf(0.52 + random.nextDouble() * 0.05));
        }
        
        return prices;
    }
}
