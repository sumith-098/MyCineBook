package com.cinebook.booking.dto;

import java.math.BigDecimal;

public class AdminBookingStatsDto {
    private long totalBookings;
    private BigDecimal totalRevenue;

    public AdminBookingStatsDto() {}
    public AdminBookingStatsDto(long totalBookings, BigDecimal totalRevenue) {
        this.totalBookings = totalBookings; this.totalRevenue = totalRevenue;
    }
    public long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(long totalBookings) { this.totalBookings = totalBookings; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}
