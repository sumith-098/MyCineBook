package com.cinebook.auth.service;

import com.cinebook.auth.dto.AuthResponse;
import com.cinebook.auth.dto.ChangePasswordRequest;
import com.cinebook.auth.dto.LoginRequest;
import com.cinebook.auth.entity.Admin;
import com.cinebook.auth.exception.ApiException;
import com.cinebook.auth.repository.AdminRepository;
import com.cinebook.auth.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AdminAuthService(AdminRepository adminRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest req) {
        Admin admin = adminRepository.findByEmail(req.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ApiException("Invalid credentials.", HttpStatus.UNAUTHORIZED));
        if (!passwordEncoder.matches(req.getPassword(), admin.getPasswordHash())) {
            throw new ApiException("Invalid credentials.", HttpStatus.UNAUTHORIZED);
        }
        String access = jwtUtil.generateAccessToken(admin.getAdminId(), admin.getEmail(), admin.getName(), "ADMIN");
        String refresh = jwtUtil.generateRefreshToken(admin.getAdminId(), admin.getEmail(), admin.getName(), "ADMIN");
        return new AuthResponse(access, refresh, admin.getAdminId(), admin.getName(), admin.getEmail(), "ADMIN");
    }

    @Transactional
    public void changePassword(Long adminId, ChangePasswordRequest req) {
        if (!req.getNewPassword().equals(req.getConfirmNewPassword())) {
            throw new ApiException("New passwords do not match.", HttpStatus.BAD_REQUEST);
        }
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ApiException("Admin not found.", HttpStatus.NOT_FOUND));
        if (!passwordEncoder.matches(req.getCurrentPassword(), admin.getPasswordHash())) {
            throw new ApiException("Current password is incorrect.", HttpStatus.UNAUTHORIZED);
        }
        admin.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        adminRepository.save(admin);
    }
}