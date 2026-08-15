package com.cinebook.admin.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** Singleton row (id always 1) — platform-wide commission percentage, admin-managed. */
@Entity
@Table(name = "commission_config")
public class CommissionConfig {

    @Id
    private Integer id = 1;

    @Column(name = "commission_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionPct = BigDecimal.valueOf(10.0);

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public BigDecimal getCommissionPct() { return commissionPct; }
    public void setCommissionPct(BigDecimal commissionPct) { this.commissionPct = commissionPct; }
}
