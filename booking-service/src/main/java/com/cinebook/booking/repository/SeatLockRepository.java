package com.cinebook.booking.repository;

import com.cinebook.booking.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SeatLockRepository extends JpaRepository<SeatLock, SeatLock.SeatLockId> {

    List<SeatLock> findByShowtimeId(Long showtimeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SeatLock s WHERE s.bookingGroup = :bookingGroup")
    void deleteByBookingGroup(String bookingGroup);

    @Modifying
    @Transactional
    @Query("DELETE FROM SeatLock s WHERE s.bookingId = :bookingId")
    void deleteByBookingId(Long bookingId);
}
