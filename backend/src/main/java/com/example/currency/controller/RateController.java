package com.example.currency.controller;

import com.example.currency.model.RateSnapshot;
import com.example.currency.service.RateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class RateController {

    private final RateService rateService;

    @GetMapping("/latest")
    public ResponseEntity<RateSnapshot> getLatestRates(@RequestParam(defaultValue = "USD") String base) {
        return ResponseEntity.ok(rateService.fetchLatestRates(base));
    }

    @GetMapping("/convert")
    public ResponseEntity<Map<String, Object>> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount
    ) {
        BigDecimal result = rateService.convert(from, to, amount);
        return ResponseEntity.ok(Map.of(
                "from", from,
                "to", to,
                "amount", amount,
                "result", result
        ));
    }

    @GetMapping("/historical")
    public ResponseEntity<Map<String, Object>> getHistoricalRates(
            @RequestParam String base,
            @RequestParam String target,
            @RequestParam(defaultValue = "1M") String period
    ) {
        return ResponseEntity.ok(rateService.getHistoricalRates(base, target, period));
    }

    @GetMapping("/predict")
    public ResponseEntity<Map<String, BigDecimal>> predictShortTerm(
            @RequestParam String base,
            @RequestParam String target
    ) {
        return ResponseEntity.ok(rateService.predictShortTerm(base, target));
    }
}
