package com.cinebook.booking.dto;

import java.math.BigDecimal;
import java.util.List;

public class BookingDto {
    private Long bookingId;          // first booking id in the group — used for detail/cancel/review links
    private String bookingGroup;
    private String bookingRef;
    private Long movieId;
    private String movieTitle;
    private String theaterName;
    private String location;
    private String showDate;
    private String showTime;
    private String screen;
    private List<String> seats;
    private BigDecimal totalAmount;
    private String status;           // CONFIRMED | CANCELLED | WATCHED (group-level rollup)
    private boolean hasReview;
    private String createdAt;

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingGroup() { return bookingGroup; }
    public void setBookingGroup(String bookingGroup) { this.bookingGroup = bookingGroup; }
    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }
    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isHasReview() { return hasReview; }
    public void setHasReview(boolean hasReview) { this.hasReview = hasReview; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
