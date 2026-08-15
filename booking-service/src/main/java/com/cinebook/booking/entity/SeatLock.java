package com.cinebook.booking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Objects;

/**
 * THE concurrency primitive for the whole service. One row = one seat is currently held by a
 * non-cancelled booking. The composite PRIMARY KEY (showtime_id, seat_no) is what makes seat
 * booking race-safe: InnoDB enforces PK uniqueness atomically at INSERT time, so when two
 * requests try to lock the same seat simultaneously, the database itself guarantees exactly one
 * of them succeeds — there is no "check if free, then insert" window for a race to slip through.
 * Freed by DELETE the moment a booking is cancelled (see BookingService.cancel), which is what
 * makes the seat bookable again.
 */
@Entity
@Table(name = "seat_lock")
@IdClass(SeatLock.SeatLockId.class)
public class SeatLock {

    @Id
    @Column(name = "showtime_id")
    private Long showtimeId;

    @Id
    @Column(name = "seat_no", length = 10)
    private String seatNo;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "booking_group", nullable = false, length = 20)
    private String bookingGroup;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt = LocalDateTime.now();

    public SeatLock() {}
    public SeatLock(Long showtimeId, String seatNo, Long bookingId, String bookingGroup) {
        this.showtimeId = showtimeId;
        this.seatNo = seatNo;
        this.bookingId = bookingId;
        this.bookingGroup = bookingGroup;
    }

    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public String getSeatNo() { return seatNo; }
    public void setSeatNo(String seatNo) { this.seatNo = seatNo; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getBookingGroup() { return bookingGroup; }
    public void setBookingGroup(String bookingGroup) { this.bookingGroup = bookingGroup; }
    public LocalDateTime getLockedAt() { return lockedAt; }
    public void setLockedAt(LocalDateTime lockedAt) { this.lockedAt = lockedAt; }

    public static class SeatLockId implements Serializable {
        private Long showtimeId;
        private String seatNo;

        public SeatLockId() {}
        public SeatLockId(Long showtimeId, String seatNo) { this.showtimeId = showtimeId; this.seatNo = seatNo; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SeatLockId that)) return false;
            return Objects.equals(showtimeId, that.showtimeId) && Objects.equals(seatNo, that.seatNo);
        }

        @Override
        public int hashCode() { return Objects.hash(showtimeId, seatNo); }
    }
}
