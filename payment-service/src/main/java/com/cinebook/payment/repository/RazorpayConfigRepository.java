package com.cinebook.payment.repository;

import com.cinebook.payment.entity.RazorpayConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RazorpayConfigRepository extends JpaRepository<RazorpayConfig, Integer> {
}
