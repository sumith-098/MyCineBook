package com.cinebook.admin.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CommissionConfigRequest {
    @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "100.0")
    private BigDecimal commissionPct;

    public BigDecimal getCommissionPct() { return commissionPct; }
    public void setCommissionPct(BigDecimal commissionPct) { this.commissionPct = commissionPct; }
}
