package com.example.currency.controller;

import com.example.currency.service.CryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/crypto")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class CryptoController {

    private final CryptoService cryptoService;

    @GetMapping("/prices")
    public ResponseEntity<Map<String, BigDecimal>> getCryptoPrices(
            @RequestParam(defaultValue = "USD") String currency
    ) {
        return ResponseEntity.ok(cryptoService.getCryptoPrices(currency));
    }
}
