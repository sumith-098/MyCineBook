package com.cinebook.auth.dto;

public class AdminAuthStatsDto {
    private long customerCount;
    private long activeOwnerCount;
    private long pendingOwnerCount;

    public AdminAuthStatsDto() {}
    public AdminAuthStatsDto(long customerCount, long activeOwnerCount, long pendingOwnerCount) {
        this.customerCount = customerCount; this.activeOwnerCount = activeOwnerCount; this.pendingOwnerCount = pendingOwnerCount;
    }
    public long getCustomerCount() { return customerCount; }
    public void setCustomerCount(long customerCount) { this.customerCount = customerCount; }
    public long getActiveOwnerCount() { return activeOwnerCount; }
    public void setActiveOwnerCount(long activeOwnerCount) { this.activeOwnerCount = activeOwnerCount; }
    public long getPendingOwnerCount() { return pendingOwnerCount; }
    public void setPendingOwnerCount(long pendingOwnerCount) { this.pendingOwnerCount = pendingOwnerCount; }
}
