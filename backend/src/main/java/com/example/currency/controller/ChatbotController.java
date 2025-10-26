package com.example.currency.controller;

import com.example.currency.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")  // ← Fixed this line
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> handleQuery(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        String response = chatbotService.processQuery(query);
        
        return ResponseEntity.ok(Map.of(
            "response", response,
            "timestamp", Instant.now().toString()
        ));
    }
}
