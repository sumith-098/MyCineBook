package com.cinebook.booking.repository;

import com.cinebook.booking.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByBookingId(Long bookingId);
    boolean existsByBookingId(Long bookingId);
    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    // used to build the has_review flag in one query instead of N+1
    @org.springframework.data.jpa.repository.Query("SELECT r.bookingId FROM Review r WHERE r.custId = :custId")
    Set<Long> findReviewedBookingIdsByCustId(Long custId);
}
