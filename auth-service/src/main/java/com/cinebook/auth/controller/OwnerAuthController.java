package com.cinebook.auth.controller;

import com.cinebook.auth.dto.*;
import com.cinebook.auth.exception.ApiException;
import com.cinebook.auth.service.OwnerAuthService;
import com.cinebook.auth.util.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/owner")
public class OwnerAuthController {

    private final OwnerAuthService ownerAuthService;
    private final RateLimiter rateLimiter;

    public OwnerAuthController(OwnerAuthService ownerAuthService, RateLimiter rateLimiter) {
        this.ownerAuthService = ownerAuthService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@Valid @RequestBody RegisterRequest req) {
        String devOtp = ownerAuthService.register(req);
        var data = devOtp != null ? Map.of("devOtp", devOtp) : Map.<String, String>of();
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to " + req.getEmail() + ".", data));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        ownerAuthService.verifyRegisterOtp(req);
        return ResponseEntity.ok(ApiResponse.ok("Registration done! Account under review — admin will approve within 24 hours."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req,
                                                             HttpServletRequest http) {
        if (rateLimiter.isBlocked(http.getRemoteAddr() + ":owner_login")) {
            throw new ApiException("Too many attempts. Wait 5 minutes.", HttpStatus.TOO_MANY_REQUESTS);
        }
        AuthResponse tokens = ownerAuthService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Welcome, " + tokens.getName() + "!", tokens));
    }
           @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        String devOtp = ownerAuthService.forgotPassword(req.getEmail());
        var data = devOtp != null ? Map.of("devOtp", devOtp) : Map.<String, String>of();
        return ResponseEntity.ok(ApiResponse.ok("If this email is registered, you will receive an OTP.", data));
    }

        @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyResetOtp(@Valid @RequestBody VerifyOtpRequest req) {
        String resetToken = ownerAuthService.verifyResetOtp(req);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified.", Map.of("resetToken", resetToken)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        ownerAuthService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.ok("Password updated! Please log in."));
    }

    /** Sanity endpoint other services / the frontend can call to confirm the current JWT is a valid, logged-in owner. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("OK", Map.of("ownerId", auth.getPrincipal())));
    }
}
