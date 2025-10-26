package com.example.currency.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsService {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.news-api.key}")
    private String apiKey;

    @Value("${app.news-api.base-url}")
    private String apiBaseUrl;

    @Value("${app.mock-mode}")
    private boolean mockMode;

    // In-memory cache
    private List<Map<String, Object>> cachedNews;
    private LocalDateTime lastFetch;

    public List<Map<String, Object>> getForexNews() {
        // Check in-memory cache (30 min)
        if (cachedNews != null && lastFetch != null && 
            lastFetch.plusMinutes(30).isAfter(LocalDateTime.now())) {
            log.debug("Returning cached news");
            return cachedNews;
        }

        if (mockMode) {
            List<Map<String, Object>> news = generateMockNews();
            cachedNews = news;
            lastFetch = LocalDateTime.now();
            return news;
        }

        try {
            WebClient webClient = webClientBuilder.build();
            JsonNode response = webClient.get()
                    .uri(apiBaseUrl + "/everything?q=forex+OR+currency+OR+exchange+rate&sortBy=publishedAt&pageSize=10&apiKey=" + apiKey)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<Map<String, Object>> articles = new ArrayList<>();
            if (response != null && response.has("articles")) {
                response.get("articles").forEach(article -> {
                    Map<String, Object> newsItem = new HashMap<>();
                    newsItem.put("title", article.get("title").asText());
                    newsItem.put("description", article.has("description") ? article.get("description").asText() : "");
                    newsItem.put("url", article.get("url").asText());
                    newsItem.put("publishedAt", article.get("publishedAt").asText());
                    newsItem.put("source", article.get("source").get("name").asText());
                    articles.add(newsItem);
                });
                
                cachedNews = articles;
                lastFetch = LocalDateTime.now();
                log.info("Successfully fetched {} news articles", articles.size());
                return articles;
            }
        } catch (Exception e) {
            log.error("Error fetching news from API", e);
        }

        // Fallback to mock
        List<Map<String, Object>> news = generateMockNews();
        cachedNews = news;
        lastFetch = LocalDateTime.now();
        return news;
    }

    private List<Map<String, Object>> generateMockNews() {
        List<Map<String, Object>> articles = new ArrayList<>();
        
        articles.add(createMockArticle(
            "Dollar Strengthens Against Major Currencies",
            "The US dollar rose today as investors weighed economic data...",
            "Financial Times"
        ));
        
        articles.add(createMockArticle(
            "EUR/USD Falls Below Key Support Level",
            "Euro weakened against the dollar amid European economic concerns...",
            "Reuters"
        ));
        
        articles.add(createMockArticle(
            "Indian Rupee Holds Steady Despite Global Volatility",
            "The Indian rupee remained stable at around 83.40 per dollar...",
            "Economic Times"
        ));
        
        articles.add(createMockArticle(
            "Central Bank Signals Interest Rate Changes",
            "Markets react to hints of potential monetary policy shifts...",
            "Bloomberg"
        ));
        
        articles.add(createMockArticle(
            "Forex Markets Show Mixed Signals Amid Economic Uncertainty",
            "Currency traders navigate complex landscape of global indicators...",
            "Wall Street Journal"
        ));
        
        return articles;
    }

    private Map<String, Object> createMockArticle(String title, String description, String source) {
        Map<String, Object> article = new HashMap<>();
        article.put("title", title);
        article.put("description", description);
        article.put("url", "https://example.com/news");
        article.put("publishedAt", LocalDateTime.now().minusHours(new Random().nextInt(24)).toString());
        article.put("source", source);
        return article;
    }
}
