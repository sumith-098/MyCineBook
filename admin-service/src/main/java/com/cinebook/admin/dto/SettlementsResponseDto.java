package com.cinebook.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public class SettlementsResponseDto {
    private BigDecimal commissionPct;
    private List<OwnerSettlementSummaryDto> owners;
    private List<SettlementDto> recentSettlements;

    public BigDecimal getCommissionPct() { return commissionPct; }
    public void setCommissionPct(BigDecimal commissionPct) { this.commissionPct = commissionPct; }
    public List<OwnerSettlementSummaryDto> getOwners() { return owners; }
    public void setOwners(List<OwnerSettlementSummaryDto> owners) { this.owners = owners; }
    public List<SettlementDto> getRecentSettlements() { return recentSettlements; }
    public void setRecentSettlements(List<SettlementDto> recentSettlements) { this.recentSettlements = recentSettlements; }
}
