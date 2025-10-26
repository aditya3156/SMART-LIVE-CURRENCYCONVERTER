package com.example.currency.controller;

import com.example.currency.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/latest")
    public ResponseEntity<List<Map<String, Object>>> getLatestNews() {
        return ResponseEntity.ok(newsService.getForexNews());
    }
}
