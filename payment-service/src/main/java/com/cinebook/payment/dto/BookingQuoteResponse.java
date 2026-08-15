package com.cinebook.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Mirrors booking-service's BookingQuoteDto — only the fields payment-service needs. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingQuoteResponse {
    private Long movieId;
    private String movieTitle;
    private Long showtimeId;
    private BigDecimal totalAmount;

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
