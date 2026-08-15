package com.cinebook.auth.repository;

import com.cinebook.auth.entity.TheaterOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TheaterOwnerRepository extends JpaRepository<TheaterOwner, Long> {
    Optional<TheaterOwner> findByEmail(String email);
    Optional<TheaterOwner> findByEmailAndIsActiveTrue(String email);
    boolean existsByEmail(String email);
    List<TheaterOwner> findByIsActiveFalseOrderByCreatedAtDesc();
    List<TheaterOwner> findByIsActiveTrueOrderByCreatedAtDesc();
    long countByIsActiveTrue();
    long countByIsActiveFalse();
}
