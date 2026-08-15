package com.cinebook.auth.service;

import com.cinebook.auth.dto.*;
import com.cinebook.auth.entity.Customer;
import com.cinebook.auth.entity.OtpStore;
import com.cinebook.auth.exception.ApiException;
import com.cinebook.auth.repository.CustomerRepository;
import com.cinebook.auth.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerAuthService {

    private final CustomerRepository customerRepository;
    private final OtpService otpService;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public CustomerAuthService(CustomerRepository customerRepository, OtpService otpService,
                                EmailService emailService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.otpService = otpService;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /** Returns the OTP only when app.mail.dev-mode=true (frontend shows it inline instead of requiring email). */
    public String register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ApiException("Passwords do not match.", HttpStatus.BAD_REQUEST);
        }
        if (customerRepository.existsByEmail(email)) {
            throw new ApiException("Email already registered. Please log in.", HttpStatus.CONFLICT);
        }

        String hashedPwd = passwordEncoder.encode(req.getPassword());
        String data = req.getName() + "|" + hashedPwd + "|" + (req.getPhone() == null ? "" : req.getPhone());
        String otp = otpService.issueOtp(email, "register", data);

        boolean sent = emailService.sendOtpEmail(email, otp, req.getName());
        if (!sent && !emailService.isDevMode()) {
            throw new ApiException("Could not send OTP email. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return emailService.isDevMode() ? otp : null;
    }

    @Transactional
    public AuthResponse verifyRegisterOtp(VerifyOtpRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        OtpStore row = otpService.verifyAndConsume(email, "register", req.getOtp().trim());

        String[] parts = row.getData().split("\\|", -1);
        String name = parts[0];
        String hashedPwd = parts[1];
        String phone = parts.length > 2 ? parts[2] : "";

        Customer customer = new Customer();
        customer.setCustName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setPasswordHash(hashedPwd);
        customer.setIsVerified(true);
        customer = customerRepository.save(customer);

        return issueTokens(customer);
    }

    public String resendOtp(String email, String purpose) {
        email = email.trim().toLowerCase();
        Customer customer = customerRepository.findByEmail(email).orElse(null);
        String name = customer != null ? customer.getCustName() : "";
        String otp = otpService.regenerate(email, purpose); // keeps the pending registration payload intact
        boolean sent = emailService.sendOtpEmail(email, otp, name);
        if (!sent && !emailService.isDevMode()) {
            throw new ApiException("Could not resend OTP.", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return emailService.isDevMode() ? otp : null;
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Invalid email or account not verified.", HttpStatus.UNAUTHORIZED));

        if (customer.getIsVerified() == null || !customer.getIsVerified()) {
            throw new ApiException("Invalid email or account not verified.", HttpStatus.UNAUTHORIZED);
        }
        if (!passwordEncoder.matches(req.getPassword(), customer.getPasswordHash())) {
            throw new ApiException("Incorrect password.", HttpStatus.UNAUTHORIZED);
        }
        return issueTokens(customer);
    }

    public String forgotPassword(String emailRaw) {
        String email = emailRaw.trim().toLowerCase();
        Customer customer = customerRepository.findByEmail(email).orElse(null);
        // Security: same response whether or not the email exists — avoids account enumeration
        if (customer == null) return null;

        String otp = otpService.issueOtp(email, "reset", "");
        boolean sent = emailService.sendOtpEmail(email, otp, customer.getCustName());
        if (!sent && !emailService.isDevMode()) {
            throw new ApiException("Could not send OTP email. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
        }
        return emailService.isDevMode() ? otp : null;
    }

    /** Verifies the reset OTP and returns a short-lived reset token the client must send back to /reset-password. */
    public String verifyResetOtp(VerifyOtpRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        otpService.verifyAndConsume(email, "reset", req.getOtp().trim());
        return jwtUtil.generatePasswordResetToken(email);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new ApiException("Passwords do not match.", HttpStatus.BAD_REQUEST);
        }
        jwtUtil.validatePasswordResetToken(req.getResetToken(), email); // throws if invalid/expired/mismatched

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Account not found.", HttpStatus.NOT_FOUND));
        customer.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        customerRepository.save(customer);
    }

    private AuthResponse issueTokens(Customer customer) {
        String access = jwtUtil.generateAccessToken(customer.getCustId(), customer.getEmail(), customer.getCustName(), "CUSTOMER");
        String refresh = jwtUtil.generateRefreshToken(customer.getCustId(), customer.getEmail(), customer.getCustName(), "CUSTOMER");
        return new AuthResponse(access, refresh, customer.getCustId(), customer.getCustName(), customer.getEmail(), "CUSTOMER");
    }
}
