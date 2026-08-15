package com.cinebook.booking.dto;

import java.math.BigDecimal;
import java.util.List;

public class BookingQuoteDto {
    private Long movieId;
    private String movieTitle;
    private Long showtimeId;
    private String showDate;
    private String showTime;
    private String screen;
    private String theaterName;
    private String location;
    private List<SeatQuoteDto> seatDetails;
    private BigDecimal totalAmount;

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public List<SeatQuoteDto> getSeatDetails() { return seatDetails; }
    public void setSeatDetails(List<SeatQuoteDto> seatDetails) { this.seatDetails = seatDetails; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
