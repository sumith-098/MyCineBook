package com.cinebook.booking.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Deliberately denormalized: movie/theater/showtime display fields are snapshotted here at
 * booking time (a copy, not a live join to catalog-service). Two reasons:
 *  1) my-bookings / booking-detail reads never need a cross-service call.
 *  2) A booking's displayed price/seat/category can never silently drift if the owner edits
 *     the screen layout or catalog data later — same guarantee the original app relied on.
 */
@Entity
@Table(name = "booking", indexes = {
        @Index(name = "idx_booking_cust", columnList = "custId"),
        @Index(name = "idx_booking_group", columnList = "bookingGroup"),
        @Index(name = "idx_booking_showtime", columnList = "showtimeId")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "cust_id", nullable = false)
    private Long custId;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "movie_title", length = 150)
    private String movieTitle;

    @Column(name = "theater_id", nullable = false)
    private Long theaterId;

    @Column(name = "theater_name", length = 100)
    private String theaterName;

    @Column(length = 150)
    private String location;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Column(name = "show_date")
    private LocalDate showDate;

    @Column(name = "show_time")
    private LocalTime showTime;

    @Column(length = 50)
    private String screen;

    @Column(name = "seat_no", nullable = false, length = 10)
    private String seatNo;

    @Column(name = "seat_category", length = 50)
    private String seatCategory;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal amount;

    @Column(name = "booking_ref", nullable = false, unique = true, length = 40)
    private String bookingRef;

    @Column(name = "booking_group", nullable = false, length = 20)
    private String bookingGroup;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod = "cash";

    @Column(name = "payment_reference", length = 100)
    private String paymentReference; // e.g. razorpay payment id, null for cash/counter

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getCustId() { return custId; }
    public void setCustId(Long custId) { this.custId = custId; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getShowTime() { return showTime; }
    public void setShowTime(LocalTime showTime) { this.showTime = showTime; }
    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }
    public String getSeatNo() { return seatNo; }
    public void setSeatNo(String seatNo) { this.seatNo = seatNo; }
    public String getSeatCategory() { return seatCategory; }
    public void setSeatCategory(String seatCategory) { this.seatCategory = seatCategory; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
    public String getBookingGroup() { return bookingGroup; }
    public void setBookingGroup(String bookingGroup) { this.bookingGroup = bookingGroup; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
