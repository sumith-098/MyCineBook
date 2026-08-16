package com.cinebook.auth.controller;

import com.cinebook.auth.dto.*;
import com.cinebook.auth.entity.TheaterOwner;
import com.cinebook.auth.exception.ApiException;
import com.cinebook.auth.repository.CustomerRepository;
import com.cinebook.auth.service.AdminAuthService;
import com.cinebook.auth.service.OwnerAuthService;
import com.cinebook.auth.util.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/admin")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final OwnerAuthService ownerAuthService;
    private final CustomerRepository customerRepository;
    private final RateLimiter rateLimiter;

    public AdminAuthController(AdminAuthService adminAuthService, OwnerAuthService ownerAuthService,
                                CustomerRepository customerRepository, RateLimiter rateLimiter) {
        this.adminAuthService = adminAuthService;
        this.ownerAuthService = ownerAuthService;
        this.customerRepository = customerRepository;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req,
                                                             HttpServletRequest http) {
        if (rateLimiter.isBlocked(http.getRemoteAddr() + ":admin_login", 5, 10)) {
            throw new ApiException("Too many attempts.", HttpStatus.TOO_MANY_REQUESTS);
        }
        AuthResponse tokens = adminAuthService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Welcome, Admin!", tokens));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(Authentication auth,
                                                              @Valid @RequestBody ChangePasswordRequest req) {
        Long adminId = (Long) auth.getPrincipal();
        adminAuthService.changePassword(adminId, req);
        return ResponseEntity.ok(ApiResponse.ok("Password updated."));
    }

    @GetMapping("/owners/pending")
    public ResponseEntity<ApiResponse<List<TheaterOwner>>> pendingOwners() {
        return ResponseEntity.ok(ApiResponse.ok("OK", ownerAuthService.listPending()));
    }

    @GetMapping("/owners/active")
    public ResponseEntity<ApiResponse<List<AdminOwnerDto>>> activeOwners() {
        List<AdminOwnerDto> owners = ownerAuthService.listActive().stream()
                .map(o -> new AdminOwnerDto(o.getOwnerId(), o.getName(), o.getEmail()))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("OK", owners));
    }

    @PostMapping("/owners/{ownerId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveOwner(@PathVariable Long ownerId) {
        ownerAuthService.approve(ownerId);
        return ResponseEntity.ok(ApiResponse.ok("Owner approved and notified."));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminAuthStatsDto>> stats() {
        AdminAuthStatsDto dto = new AdminAuthStatsDto(
                customerRepository.count(),
                ownerAuthService.listActive().size(),
                ownerAuthService.listPending().size()
        );
        return ResponseEntity.ok(ApiResponse.ok("OK", dto));
    }
}