package com.cinebook.admin.repository;

import com.cinebook.admin.entity.Settlement;
import com.cinebook.admin.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findTop20ByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(s.amount),0) FROM Settlement s WHERE s.ownerId = :ownerId AND s.status = :status")
    BigDecimal sumByOwnerIdAndStatus(Long ownerId, SettlementStatus status);
}
