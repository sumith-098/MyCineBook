package com.cinebook.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateBookingRequest {
    @NotNull(message = "movieId is required")
    private Long movieId;

    @NotNull(message = "showtimeId is required")
    private Long showtimeId;

    @NotEmpty(message = "Select at least one seat")
    private List<String> seats;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod; // "cash" only for now — see BookingService.book()

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
