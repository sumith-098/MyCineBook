package com.cinebook.booking.dto;

import java.math.BigDecimal;

public class SeatQuoteDto {
    private String seat;
    private String category;
    private BigDecimal price;

    public SeatQuoteDto() {}
    public SeatQuoteDto(String seat, String category, BigDecimal price) {
        this.seat = seat; this.category = category; this.price = price;
    }
    public String getSeat() { return seat; }
    public void setSeat(String seat) { this.seat = seat; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
