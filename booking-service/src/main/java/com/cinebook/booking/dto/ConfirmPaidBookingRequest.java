package com.cinebook.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Sent only by payment-service, only after it has independently verified a Razorpay signature. */
public class ConfirmPaidBookingRequest {
    @NotNull private Long custId;
    @NotBlank private String custEmail;
    private String custName;
    @NotNull private Long movieId;
    @NotNull private Long showtimeId;
    @NotEmpty private List<String> seats;
    @NotBlank private String paymentReference;

    public Long getCustId() { return custId; }
    public void setCustId(Long custId) { this.custId = custId; }
    public String getCustEmail() { return custEmail; }
    public void setCustEmail(String custEmail) { this.custEmail = custEmail; }
    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
}
