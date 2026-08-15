package com.cinebook.auth.controller;

import com.cinebook.auth.dto.*;
import com.cinebook.auth.exception.ApiException;
import com.cinebook.auth.service.CustomerAuthService;
import com.cinebook.auth.util.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/customer")
public class CustomerAuthController {

    private final CustomerAuthService customerAuthService;
    private final RateLimiter rateLimiter;

    public CustomerAuthController(CustomerAuthService customerAuthService, RateLimiter rateLimiter) {
        this.customerAuthService = customerAuthService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@Valid @RequestBody RegisterRequest req,
                                                                       HttpServletRequest http) {
        if (rateLimiter.isBlocked(http.getRemoteAddr() + ":register")) {
            throw new ApiException("Too many attempts. Please wait 5 minutes.", HttpStatus.TOO_MANY_REQUESTS);
        }
        String devOtp = customerAuthService.register(req);
        var data = devOtp != null ? Map.of("devOtp", devOtp) : Map.<String, String>of();
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to " + req.getEmail() + ". Valid for 10 minutes.", data));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        AuthResponse tokens = customerAuthService.verifyRegisterOtp(req);
        return ResponseEntity.ok(ApiResponse.ok("Welcome to CineBook, " + tokens.getName() + "!", tokens));
    }

    @PostMapping("/resend-otp/{purpose}")
    public ResponseEntity<ApiResponse<Map<String, String>>> resendOtp(@PathVariable String purpose,
                                                                        @RequestParam String email) {
        String devOtp = customerAuthService.resendOtp(email, purpose);
        var data = devOtp != null ? Map.of("devOtp", devOtp) : Map.<String, String>of();
        return ResponseEntity.ok(ApiResponse.ok("New OTP sent!", data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req,
                                                             HttpServletRequest http) {
        if (rateLimiter.isBlocked(http.getRemoteAddr() + ":login")) {
            throw new ApiException("Too many login attempts. Please wait 5 minutes.", HttpStatus.TOO_MANY_REQUESTS);
        }
        AuthResponse tokens = customerAuthService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Welcome back, " + tokens.getName() + "!", tokens));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        String devOtp = customerAuthService.forgotPassword(req.getEmail());
        var data = devOtp != null ? Map.of("devOtp", devOtp) : Map.<String, String>of();
        // Same message regardless of whether the email exists — avoids account enumeration
        return ResponseEntity.ok(ApiResponse.ok("If this email is registered, you will receive an OTP.", data));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyResetOtp(@Valid @RequestBody VerifyOtpRequest req) {
        String resetToken = customerAuthService.verifyResetOtp(req);
        return ResponseEntity.ok(ApiResponse.ok("OTP verified.", Map.of("resetToken", resetToken)));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        customerAuthService.resetPassword(req);
        return ResponseEntity.ok(ApiResponse.ok("Password updated! Please log in."));
    }
}
