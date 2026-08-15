package com.cinebook.payment.controller;

import com.cinebook.payment.dto.ApiResponse;
import com.cinebook.payment.dto.RazorpayConfigRequest;
import com.cinebook.payment.service.RazorpayConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/admin")
public class AdminPaymentConfigController {

    private final RazorpayConfigService configService;

    public AdminPaymentConfigController(RazorpayConfigService configService) {
        this.configService = configService;
    }

    /** keyId only — the secret is NEVER exposed by any endpoint, even to admins, once saved. */
    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getConfig() {
        String keyId = configService.getPublicKeyIdOrNull();
        return ApiResponse.ok("OK", Map.of("keyId", keyId == null ? "" : keyId, "configured", keyId != null));
    }

    @PutMapping("/config")
    public ApiResponse<Void> saveConfig(@Valid @RequestBody RazorpayConfigRequest req) {
        configService.save(req.getKeyId(), req.getKeySecret());
        return ApiResponse.ok("Razorpay configuration saved.");
    }
}
