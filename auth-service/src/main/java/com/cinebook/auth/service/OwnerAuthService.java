package com.cinebook.auth.service;

import com.cinebook.auth.dto.*;
import com.cinebook.auth.entity.OtpStore;
import com.cinebook.auth.entity.TheaterOwner;
import com.cinebook.auth.exception.ApiException;
import com.cinebook.auth.repository.TheaterOwnerRepository;
import com.cinebook.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OwnerAuthService {

    private final TheaterOwnerRepository ownerRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // @Value("${app.cors.allowed-origins}")
    // private String frontendOrigin; // first entry used to build the "log in" link in the approval email
    private String frontendOrigin = "https://cinebook-frontend.lemondesert-03dc2ff4.eastasia.azurecontainerapps.io";
    public OwnerAuthService(TheaterOwnerRepository ownerRepository, OtpService otpService,
                             EmailService emailService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.ownerRepository = ownerRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public String register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (ownerRepository.existsByEmail(email)) {
            throw new ApiException("Email already registered.", HttpStatus.CONFLICT);
        }
        String hashedPwd = passwordEncoder.encode(req.getPassword());
        String data = req.getName() + "|" + hashedPwd + "|" + (req.getPhone() == null ? "" : req.getPhone());
        String otp = otpService.issueOtp(email, "owner_reg", data);

        boolean sent = emailService.sendOtpEmail(email, otp, req.getName());
        if (!sent && !emailService.isDevMode()) {
            throw new ApiException("Could not send OTP email. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return emailService.isDevMode() ? otp : null;
    }

    @Transactional
    public void verifyRegisterOtp(VerifyOtpRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        OtpStore row = otpService.verifyAndConsume(email, "owner_reg", req.getOtp().trim());

        String[] parts = row.getData().split("\\|", -1);
        String name = parts[0];
        String hashedPwd = parts[1];
        String phone = parts.length > 2 ? parts[2] : "";

        TheaterOwner owner = new TheaterOwner();
        owner.setName(name);
        owner.setEmail(email);
        owner.setPhone(phone);
        owner.setPasswordHash(hashedPwd);
        owner.setIsActive(false); // pending admin approval, same as the Flask app
        ownerRepository.save(owner);
        // No tokens issued here — owner must wait for admin approval, then log in normally.
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        TheaterOwner owner = ownerRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new ApiException("Invalid credentials or account not yet approved.", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(req.getPassword(), owner.getPasswordHash())) {
            throw new ApiException("Invalid credentials or account not yet approved.", HttpStatus.UNAUTHORIZED);
        }
        String access = jwtUtil.generateAccessToken(owner.getOwnerId(), owner.getEmail(), owner.getName(), "OWNER");
        String refresh = jwtUtil.generateRefreshToken(owner.getOwnerId(), owner.getEmail(), owner.getName(), "OWNER");
        return new AuthResponse(access, refresh, owner.getOwnerId(), owner.getName(), owner.getEmail(), "OWNER");
    }
      
    public String forgotPassword(String emailRaw) {
        String email = emailRaw.trim().toLowerCase();
        TheaterOwner owner = ownerRepository.findByEmail(email).orElse(null);
        if (owner == null) return null;

        String otp = otpService.issueOtp(email, "owner_reset", "");
        boolean sent = emailService.sendOtpEmail(email, otp, owner.getName());
        if (!sent && !emailService.isDevMode()) {
            throw new ApiException("Could not send OTP email. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return emailService.isDevMode() ? otp : null;
    }

    public String verifyResetOtp(VerifyOtpRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        otpService.verifyAndConsume(email, "owner_reset", req.getOtp().trim());
        return jwtUtil.generatePasswordResetToken(email);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ApiException("Passwords do not match.", HttpStatus.BAD_REQUEST);
        }
        jwtUtil.validatePasswordResetToken(req.getResetToken(), email);

        TheaterOwner owner = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Account not found.", HttpStatus.NOT_FOUND));
        owner.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        ownerRepository.save(owner);
    }
    // ── used by AdminAuthController (admin-only endpoints) ──────────────────
    public List<TheaterOwner> listPending() {
        return ownerRepository.findByIsActiveFalseOrderByCreatedAtDesc();
    }

    public List<TheaterOwner> listActive() {
        return ownerRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional
    public void approve(Long ownerId) {
        TheaterOwner owner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ApiException("Owner not found.", HttpStatus.NOT_FOUND));
        owner.setIsActive(true);
        ownerRepository.save(owner);
        //String loginUrl = frontendOrigin.split(",")[0].trim() + "/owner/login";
        emailService.sendOwnerApprovedEmail(owner.getEmail(), owner.getName(), frontendOrigin);
    }
}
