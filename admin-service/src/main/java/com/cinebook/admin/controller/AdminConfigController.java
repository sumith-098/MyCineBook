package com.cinebook.admin.controller;

import com.cinebook.admin.dto.ApiResponse;
import com.cinebook.admin.dto.CommissionConfigRequest;
import com.cinebook.admin.service.CommissionConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/config")
public class AdminConfigController {

    private final CommissionConfigService commissionConfigService;

    public AdminConfigController(CommissionConfigService commissionConfigService) {
        this.commissionConfigService = commissionConfigService;
    }

    @GetMapping
    public ApiResponse<Map<String, BigDecimal>> get() {
        return ApiResponse.ok("OK", Map.of("commissionPct", commissionConfigService.getCommissionPct()));
    }

    @PutMapping
    public ApiResponse<Void> update(@Valid @RequestBody CommissionConfigRequest req) {
        commissionConfigService.setCommissionPct(req.getCommissionPct());
        return ApiResponse.ok("Commission rate updated.");
    }
}
