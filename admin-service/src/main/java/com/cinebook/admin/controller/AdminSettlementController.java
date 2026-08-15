package com.cinebook.admin.controller;

import com.cinebook.admin.dto.*;
import com.cinebook.admin.service.SettlementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settlements")
public class AdminSettlementController {

    private final SettlementService settlementService;

    public AdminSettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public ApiResponse<SettlementsResponseDto> summary(HttpServletRequest request) {
        return ApiResponse.ok("OK", settlementService.summary(request.getHeader("Authorization")));
    }

    @PostMapping("/{ownerId}/pay")
    public ApiResponse<SettlementDto> markPaid(@PathVariable Long ownerId, @Valid @RequestBody MarkSettlementPaidRequest req) {
        SettlementDto dto = settlementService.markPaid(ownerId, req);
        return ApiResponse.ok("Settlement of ₹" + req.getAmount() + " marked as paid.", dto);
    }
}
