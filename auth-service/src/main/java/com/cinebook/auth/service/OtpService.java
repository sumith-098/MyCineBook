package com.cinebook.auth.service;

import com.cinebook.auth.entity.OtpStore;
import com.cinebook.auth.exception.ApiException;
import com.cinebook.auth.repository.OtpStoreRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final OtpStoreRepository otpStoreRepository;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.expiry-minutes:10}")
    private int expiryMinutes;

    public OtpService(OtpStoreRepository otpStoreRepository) {
        this.otpStoreRepository = otpStoreRepository;
    }

    public String generateOtp() {
        return String.valueOf(100000 + random.nextInt(900000));
    }

    @Transactional
    public String issueOtp(String email, String purpose, String data) {
        otpStoreRepository.deleteByEmailAndPurpose(email, purpose);
        String otp = generateOtp();
        OtpStore row = new OtpStore();
        row.setEmail(email);
        row.setOtp(otp);
        row.setPurpose(purpose);
        row.setData(data);
        row.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        otpStoreRepository.save(row);
        return otp;
    }

    @Transactional
    public OtpStore verifyAndConsume(String email, String purpose, String otpEntered) {
        OtpStore row = otpStoreRepository.findFirstByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new ApiException("OTP not found. Please request a new one.", HttpStatus.BAD_REQUEST));

        if (LocalDateTime.now().isAfter(row.getExpiresAt())) {
            otpStoreRepository.deleteByEmailAndPurpose(email, purpose);
            throw new ApiException("OTP expired. Please request a new one.", HttpStatus.BAD_REQUEST);
        }
        if (!row.getOtp().equals(otpEntered)) {
            throw new ApiException("Incorrect OTP.", HttpStatus.BAD_REQUEST);
        }
        otpStoreRepository.deleteByEmailAndPurpose(email, purpose);
        return row;
    }

    /** Re-rolls the OTP code for an existing pending record, keeping its "data" payload intact. */
    @Transactional
    public String regenerate(String email, String purpose) {
        OtpStore row = otpStoreRepository.findFirstByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new ApiException("No pending request found. Please start again.", HttpStatus.BAD_REQUEST));
        String otp = generateOtp();
        row.setOtp(otp);
        row.setExpiresAt(LocalDateTime.now().plusMinutes(expiryMinutes));
        otpStoreRepository.save(row);
        return otp;
    }

    // Runs every 30 minutes — clears out expired OTP rows so the table doesn't grow forever
    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 30 * 60 * 1000)
    @Transactional
    public void purgeExpired() {
        otpStoreRepository.findByExpiresAtBefore(LocalDateTime.now()).forEach(otpStoreRepository::delete);
    }
}
