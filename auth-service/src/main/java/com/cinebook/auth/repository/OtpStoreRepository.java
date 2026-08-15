package com.cinebook.auth.repository;

import com.cinebook.auth.entity.OtpStore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OtpStoreRepository extends JpaRepository<OtpStore, Long> {
    Optional<OtpStore> findFirstByEmailAndPurposeOrderByCreatedAtDesc(String email, String purpose);
    void deleteByEmailAndPurpose(String email, String purpose);
    List<OtpStore> findByExpiresAtBefore(java.time.LocalDateTime cutoff);
}
