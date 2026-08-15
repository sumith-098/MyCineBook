package com.cinebook.booking.service;

import com.cinebook.booking.dto.AddReviewRequest;
import com.cinebook.booking.dto.MovieReviewsDto;
import com.cinebook.booking.dto.ReviewDto;
import com.cinebook.booking.entity.Booking;
import com.cinebook.booking.entity.BookingStatus;
import com.cinebook.booking.entity.Review;
import com.cinebook.booking.exception.ApiException;
import com.cinebook.booking.repository.BookingRepository;
import com.cinebook.booking.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public ReviewDto add(Long custId, String custName, Long bookingId, AddReviewRequest req) {
        Booking booking = bookingRepository.findByBookingIdAndCustId(bookingId, custId)
                .orElseThrow(() -> new ApiException("Booking not found.", HttpStatus.NOT_FOUND));

        if (booking.getStatus() != BookingStatus.WATCHED && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ApiException("You can only review after watching the movie.", HttpStatus.BAD_REQUEST);
        }
        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new ApiException("You have already reviewed this booking.", HttpStatus.CONFLICT);
        }

        Review review = new Review();
        review.setBookingId(bookingId);
        review.setCustId(custId);
        review.setCustName(custName);
        review.setMovieId(booking.getMovieId());
        review.setRating(req.getRating());
        review.setReviewText(req.getReviewText() == null ? "" : req.getReviewText().trim());
        review = reviewRepository.save(review);
        return toDto(review);
    }

    public MovieReviewsDto forMovie(Long movieId) {
        List<Review> reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId);
        MovieReviewsDto dto = new MovieReviewsDto();
        dto.setReviews(reviews.stream().map(this::toDto).toList());
        dto.setTotalReviews((long) reviews.size());
        dto.setAverageRating(reviews.isEmpty() ? null
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0));
        return dto;
    }

    private ReviewDto toDto(Review r) {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(r.getReviewId());
        dto.setCustName(r.getCustName());
        dto.setRating(r.getRating());
        dto.setReviewText(r.getReviewText());
        dto.setCreatedAt(r.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return dto;
    }
}
