package com.cinebook.payment.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Note: NO "amount" field here on purpose — the original app trusted a client-submitted
 *  amount when creating the Razorpay order, which meant a tampered request could pay ₹1 for
 *  any number of tickets. This service always re-derives the amount server-side by calling
 *  booking-service's /api/bookings/quote instead of ever accepting one from the client. */
public class CreateOrderRequest {
    @NotNull(message = "movieId is required")
    private Long movieId;

    @NotNull(message = "showtimeId is required")
    private Long showtimeId;

    @NotEmpty(message = "Select at least one seat")
    private List<String> seats;

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }
}
