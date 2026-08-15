package com.cinebook.admin.dto;

import java.math.BigDecimal;

public class OwnerSettlementSummaryDto {
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private BigDecimal totalEarned;
    private BigDecimal adminCut;
    private BigDecimal ownerShare;
    private BigDecimal alreadyPaid;
    private BigDecimal pendingAmount;

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerEmail() { return ownerEmail; }
    public void setOwnerEmail(String ownerEmail) { this.ownerEmail = ownerEmail; }
    public BigDecimal getTotalEarned() { return totalEarned; }
    public void setTotalEarned(BigDecimal totalEarned) { this.totalEarned = totalEarned; }
    public BigDecimal getAdminCut() { return adminCut; }
    public void setAdminCut(BigDecimal adminCut) { this.adminCut = adminCut; }
    public BigDecimal getOwnerShare() { return ownerShare; }
    public void setOwnerShare(BigDecimal ownerShare) { this.ownerShare = ownerShare; }
    public BigDecimal getAlreadyPaid() { return alreadyPaid; }
    public void setAlreadyPaid(BigDecimal alreadyPaid) { this.alreadyPaid = alreadyPaid; }
    public BigDecimal getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(BigDecimal pendingAmount) { this.pendingAmount = pendingAmount; }
}
