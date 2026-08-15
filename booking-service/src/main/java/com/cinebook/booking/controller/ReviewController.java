package com.cinebook.booking.controller;

import com.cinebook.booking.dto.*;
import com.cinebook.booking.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private Long custId(Authentication auth) { return (Long) auth.getPrincipal(); }
    private String custName(Authentication auth) {
        return ((io.jsonwebtoken.Claims) auth.getDetails()).get("name", String.class);
    }

    @PostMapping("/{bookingId}/reviews")
    public ApiResponse<ReviewDto> addReview(Authentication auth, @PathVariable Long bookingId,
                                             @Valid @RequestBody AddReviewRequest req) {
        ReviewDto dto = reviewService.add(custId(auth), custName(auth), bookingId, req);
        return ApiResponse.ok("🎬 Thanks for your review!", dto);
    }

    /** Public — movie detail pages show reviews without requiring login. */
    @GetMapping("/movies/{movieId}/reviews")
    public ApiResponse<MovieReviewsDto> movieReviews(@PathVariable Long movieId) {
        return ApiResponse.ok("OK", reviewService.forMovie(movieId));
    }
}
