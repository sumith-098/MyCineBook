package com.cinebook.booking.dto;

import java.math.BigDecimal;

public class TheaterEarningDto {
    private Long theaterId;
    private BigDecimal totalEarned;

    public TheaterEarningDto() {}
    public TheaterEarningDto(Long theaterId, BigDecimal totalEarned) {
        this.theaterId = theaterId; this.totalEarned = totalEarned;
    }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public BigDecimal getTotalEarned() { return totalEarned; }
    public void setTotalEarned(BigDecimal totalEarned) { this.totalEarned = totalEarned; }
}
