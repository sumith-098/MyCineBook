package com.cinebook.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthStatsLiteDto {
    private long customerCount;
    private long activeOwnerCount;
    private long pendingOwnerCount;

    public long getCustomerCount() { return customerCount; }
    public void setCustomerCount(long customerCount) { this.customerCount = customerCount; }
    public long getActiveOwnerCount() { return activeOwnerCount; }
    public void setActiveOwnerCount(long activeOwnerCount) { this.activeOwnerCount = activeOwnerCount; }
    public long getPendingOwnerCount() { return pendingOwnerCount; }
    public void setPendingOwnerCount(long pendingOwnerCount) { this.pendingOwnerCount = pendingOwnerCount; }
}
