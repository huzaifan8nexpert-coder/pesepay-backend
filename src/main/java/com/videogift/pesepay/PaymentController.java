package com.videogift.pesepay;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final Map<String, Map<String, Object>> payments = new ConcurrentHashMap<>();

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "pesepay-backend");
    }

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiate(@RequestBody Map<String, Object> req) {
        String reference = String.valueOf(req.getOrDefault("reference", "VG-" + System.currentTimeMillis()));

        Map<String, Object> payment = new HashMap<>();
        payment.put("reference", reference);
        payment.put("status", "PENDING");
        payment.put("request", req);
        payments.put(reference, payment);

        String returnUrl = String.valueOf(req.getOrDefault("returnUrl", ""));
        String redirectUrl = returnUrl + (returnUrl.contains("?") ? "&" : "?") + "reference=" + reference;

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("status", "PENDING");
        res.put("reference", reference);
        res.put("redirectUrl", redirectUrl);
        res.put("message", "Backend is running. Add official PesePay encrypted API call here for live payment redirect.");

        return ResponseEntity.ok(res);
    }

    @GetMapping("/status/{reference}")
    public ResponseEntity<Map<String, Object>> status(@PathVariable String reference) {
        Map<String, Object> payment = payments.getOrDefault(reference, new HashMap<>());
        if (payment.isEmpty()) {
            payment.put("reference", reference);
            payment.put("status", "PENDING");
            payment.put("message", "Payment reference not found yet.");
        }
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> callback(@RequestBody Map<String, Object> body) {
        String reference = String.valueOf(body.getOrDefault("reference", body.getOrDefault("referenceNumber", "")));
        if (!reference.isBlank()) {
            Map<String, Object> data = payments.getOrDefault(reference, new HashMap<>());
            data.putAll(body);
            data.put("status", String.valueOf(body.getOrDefault("status", "SUCCESS")));
            payments.put(reference, data);
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
