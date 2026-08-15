package com.cinebook.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDto {
    private long customerCount;
    private long theaterCount;
    private long movieCount;
    private long bookingCount;
    private BigDecimal revenue;
    private long pendingOwnerCount;
    private long pendingSettlementOwnerCount;
    private List<AdminOwnerSummaryDto> pendingOwners;
    private List<RecentBookingDto> recentBookings;

    public long getCustomerCount() { return customerCount; }
    public void setCustomerCount(long customerCount) { this.customerCount = customerCount; }
    public long getTheaterCount() { return theaterCount; }
    public void setTheaterCount(long theaterCount) { this.theaterCount = theaterCount; }
    public long getMovieCount() { return movieCount; }
    public void setMovieCount(long movieCount) { this.movieCount = movieCount; }
    public long getBookingCount() { return bookingCount; }
    public void setBookingCount(long bookingCount) { this.bookingCount = bookingCount; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public long getPendingOwnerCount() { return pendingOwnerCount; }
    public void setPendingOwnerCount(long pendingOwnerCount) { this.pendingOwnerCount = pendingOwnerCount; }
    public long getPendingSettlementOwnerCount() { return pendingSettlementOwnerCount; }
    public void setPendingSettlementOwnerCount(long pendingSettlementOwnerCount) { this.pendingSettlementOwnerCount = pendingSettlementOwnerCount; }
    public List<AdminOwnerSummaryDto> getPendingOwners() { return pendingOwners; }
    public void setPendingOwners(List<AdminOwnerSummaryDto> pendingOwners) { this.pendingOwners = pendingOwners; }
    public List<RecentBookingDto> getRecentBookings() { return recentBookings; }
    public void setRecentBookings(List<RecentBookingDto> recentBookings) { this.recentBookings = recentBookings; }
}
