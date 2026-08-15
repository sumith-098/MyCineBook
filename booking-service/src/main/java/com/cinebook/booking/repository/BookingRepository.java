package com.cinebook.booking.repository;

import com.cinebook.booking.entity.Booking;
import com.cinebook.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByCustIdOrderByCreatedAtDesc(Long custId);
    Optional<Booking> findByBookingIdAndCustId(Long bookingId, Long custId);
    List<Booking> findByBookingGroupAndCustId(String bookingGroup, Long custId);
    List<Booking> findByShowtimeIdAndStatusNot(Long showtimeId, BookingStatus status);

    // used by ShowtimeService port of catalog-service's "active bookings" check
    long countByTheaterIdAndScreenAndStatus(Long theaterId, String screen, BookingStatus status);

    List<Booking> findByStatusAndShowDateLessThanEqual(BookingStatus status, java.time.LocalDate cutoffDate);

    List<Booking> findTop10ByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(b) FROM Booking b WHERE b.status IN ('CONFIRMED','WATCHED')")
    long countActiveOrWatched();

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(b.amount),0) FROM Booking b WHERE b.status IN ('CONFIRMED','WATCHED')")
    java.math.BigDecimal sumRevenue();

    @org.springframework.data.jpa.repository.Query("SELECT b.theaterId, COALESCE(SUM(b.amount),0) FROM Booking b " +
            "WHERE b.status IN ('CONFIRMED','WATCHED') AND b.paymentMethod = 'razorpay' AND b.theaterId IN :theaterIds " +
            "GROUP BY b.theaterId")
    List<Object[]> sumEarningsByTheaterIds(List<Long> theaterIds);
}
