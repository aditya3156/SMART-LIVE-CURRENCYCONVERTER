package com.example.currency.service;

import com.example.currency.model.RateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {

    private final RateService rateService;
    private final CryptoService cryptoService;

    public String processQuery(String query) {
        String lowerQuery = query.toLowerCase().trim();
        
        try {
            // Handle greetings
            if (lowerQuery.matches("(hi|hello|hey|greetings).*")) {
                return "Hello! I can help you with:\n\n" +
                       "• Currency conversions (e.g., 'Convert 100 USD to EUR')\n" +
                       "• Cryptocurrency prices (e.g., 'What is Bitcoin price?')\n" +
                       "• Exchange rates (e.g., 'Show USD to INR rate')\n\n" +
                       "Ask me anything!";
            }
            
            // Pattern 1: "convert X FROM to TO" or "X FROM to TO"
            Pattern convertPattern = Pattern.compile("(convert\\s+)?(\\d+(?:\\.\\d+)?)\\s*([a-z]{3})\\s*(?:to|in|into)\\s*([a-z]{3})", Pattern.CASE_INSENSITIVE);
            Matcher convertMatcher = convertPattern.matcher(lowerQuery);
            
            if (convertMatcher.find()) {
                double amount = Double.parseDouble(convertMatcher.group(2));
                String fromCurrency = convertMatcher.group(3).toUpperCase();
                String toCurrency = convertMatcher.group(4).toUpperCase();
                return convertCurrency(amount, fromCurrency, toCurrency);
            }
            
            // Pattern 2: "what is X FROM in TO"
            Pattern whatIsPattern = Pattern.compile("what\\s+is\\s+(\\d+(?:\\.\\d+)?)\\s*([a-z]{3})\\s*(?:in|to)\\s*([a-z]{3})", Pattern.CASE_INSENSITIVE);
            Matcher whatIsMatcher = whatIsPattern.matcher(lowerQuery);
            
            if (whatIsMatcher.find()) {
                double amount = Double.parseDouble(whatIsMatcher.group(1));
                String fromCurrency = whatIsMatcher.group(2).toUpperCase();
                String toCurrency = whatIsMatcher.group(3).toUpperCase();
                return convertCurrency(amount, fromCurrency, toCurrency);
            }
            
            // Specific crypto price queries
            if (lowerQuery.contains("bitcoin") || lowerQuery.contains("btc")) {
                return getCryptoPrice("BTC", "Bitcoin");
            }
            if (lowerQuery.contains("ethereum") || lowerQuery.contains("eth")) {
                return getCryptoPrice("ETH", "Ethereum");
            }
            if (lowerQuery.contains("litecoin") || lowerQuery.contains("ltc")) {
                return getCryptoPrice("LTC", "Litecoin");
            }
            if (lowerQuery.contains("bnb") || lowerQuery.contains("binance")) {
                return getCryptoPrice("BNB", "BNB");
            }
            if (lowerQuery.contains("xrp") || lowerQuery.contains("ripple")) {
                return getCryptoPrice("XRP", "XRP");
            }
            
            // General crypto query
            if (lowerQuery.contains("crypto") && lowerQuery.contains("price")) {
                return getAllCryptoPrices();
            }
            
            // Exchange rate queries
            if (lowerQuery.matches(".*\\b([a-z]{3})\\s+(?:to|rate)\\s+([a-z]{3})\\b.*")) {
                Pattern ratePattern = Pattern.compile("\\b([a-z]{3})\\s+(?:to|rate)\\s+([a-z]{3})\\b", Pattern.CASE_INSENSITIVE);
                Matcher rateMatcher = ratePattern.matcher(lowerQuery);
                if (rateMatcher.find()) {
                    String from = rateMatcher.group(1).toUpperCase();
                    String to = rateMatcher.group(2).toUpperCase();
                    return getExchangeRate(from, to);
                }
            }
            
            // Show all rates
            if (lowerQuery.contains("show") && (lowerQuery.contains("rate") || lowerQuery.contains("exchange"))) {
                return getTopExchangeRates();
            }
            
            return "I can help you with:\n\n" +
                   "• Currency conversions: 'Convert 100 USD to EUR'\n" +
                   "• Crypto prices: 'What is Bitcoin price?'\n" +
                   "• Exchange rates: 'USD to INR rate'\n\n" +
                   "Try asking me a question!";
                   
        } catch (Exception e) {
            log.error("Error processing chatbot query: {}", query, e);
            return "Sorry, I couldn't understand that. Try: 'Convert 100 USD to EUR' or 'What is Bitcoin price?'";
        }
    }
    
    private String convertCurrency(double amount, String from, String to) {
        try {
            if (from.equals(to)) {
                return String.format("%.2f %s = %.2f %s (same currency)", amount, from, amount, to);
            }
            
            RateSnapshot fromRates = rateService.fetchLatestRates(from);
            BigDecimal rate = fromRates.getRates().get(to);
            
            if (rate == null) {
                return String.format("Sorry, I don't have exchange rate data for %s to %s", from, to);
            }
            
            double result = amount * rate.doubleValue();
            return String.format("%.2f %s = %.2f %s\n\nExchange rate: 1 %s = %.4f %s", 
                                 amount, from, result, to, from, rate.doubleValue(), to);
        } catch (Exception e) {
            log.error("Error converting currency", e);
            return "Sorry, I couldn't perform that conversion. Please try again.";
        }
    }
    
    private String getCryptoPrice(String symbol, String name) {
        try {
            Map<String, BigDecimal> prices = cryptoService.getCryptoPrices("USD");
            BigDecimal price = prices.get(symbol);
            
            if (price != null) {
                return String.format("%s (%s) is currently trading at $%s USD", 
                                     name, symbol, price.setScale(2, RoundingMode.HALF_UP));
            } else {
                return String.format("Sorry, I don't have current price data for %s", name);
            }
        } catch (Exception e) {
            log.error("Error fetching crypto price", e);
            return "Sorry, I couldn't fetch crypto prices right now.";
        }
    }
    
    private String getAllCryptoPrices() {
        try {
            Map<String, BigDecimal> prices = cryptoService.getCryptoPrices("USD");
            StringBuilder response = new StringBuilder("Current cryptocurrency prices:\n\n");
            
            if (prices.containsKey("BTC")) {
                response.append("• Bitcoin (BTC): $").append(prices.get("BTC").setScale(2, RoundingMode.HALF_UP)).append("\n");
            }
            if (prices.containsKey("ETH")) {
                response.append("• Ethereum (ETH): $").append(prices.get("ETH").setScale(2, RoundingMode.HALF_UP)).append("\n");
            }
            if (prices.containsKey("BNB")) {
                response.append("• BNB: $").append(prices.get("BNB").setScale(2, RoundingMode.HALF_UP)).append("\n");
            }
            if (prices.containsKey("XRP")) {
                response.append("• XRP: $").append(prices.get("XRP").setScale(2, RoundingMode.HALF_UP)).append("\n");
            }
            if (prices.containsKey("LTC")) {
                response.append("• Litecoin (LTC): $").append(prices.get("LTC").setScale(2, RoundingMode.HALF_UP)).append("\n");
            }
            
            return response.toString();
        } catch (Exception e) {
            log.error("Error fetching crypto prices", e);
            return "Sorry, I couldn't fetch crypto prices right now.";
        }
    }
    
    private String getExchangeRate(String from, String to) {
        try {
            RateSnapshot rates = rateService.fetchLatestRates(from);
            BigDecimal rate = rates.getRates().get(to);
            
            if (rate != null) {
                return String.format("Current exchange rate:\n1 %s = %.4f %s", from, rate.doubleValue(), to);
            } else {
                return String.format("Sorry, I don't have rate data for %s to %s", from, to);
            }
        } catch (Exception e) {
            log.error("Error fetching exchange rate", e);
            return "Sorry, I couldn't fetch that exchange rate.";
        }
    }
    
    private String getTopExchangeRates() {
        try {
            RateSnapshot rates = rateService.fetchLatestRates("USD");
            StringBuilder response = new StringBuilder("Top exchange rates (base: USD):\n\n");
            
            String[] topCurrencies = {"EUR", "GBP", "JPY", "INR", "AUD", "CAD", "CHF", "CNY"};
            for (String currency : topCurrencies) {
                BigDecimal rate = rates.getRates().get(currency);
                if (rate != null) {
                    response.append("• 1 USD = ").append(rate.setScale(4, RoundingMode.HALF_UP)).append(" ").append(currency).append("\n");
                }
            }
            
            return response.toString();
        } catch (Exception e) {
            log.error("Error fetching rates", e);
            return "Sorry, I couldn't fetch exchange rates right now.";
        }
    }
}
