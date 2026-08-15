package com.cinebook.catalog.service;

import com.cinebook.catalog.dto.ApiResponse;
import com.cinebook.catalog.dto.MovieReviewsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Rating (average + count) is booking-service's data (it owns reviews), fetched here purely to
 * enrich movie listings. Same soft-fail pattern as BookingServiceClient's active-count check —
 * a rating is a nice-to-have on a movie card, never worth failing the whole page load over.
 */
@Service
public class RatingClient {

    private static final Logger log = LoggerFactory.getLogger(RatingClient.class);

    private final RestClient restClient;

    public RatingClient(RestClient bookingServiceRestClient) {
        this.restClient = bookingServiceRestClient;
    }

    public record Rating(Double avgRating, Long reviewCount) {
        static final Rating EMPTY = new Rating(null, 0L);
    }

    public Rating getRating(Long movieId) {
        try {
            ApiResponse<MovieReviewsResponse> response = restClient.get()
                    .uri("/api/bookings/movies/{id}/reviews", movieId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<MovieReviewsResponse>>() {});
            if (response == null || response.getData() == null) return Rating.EMPTY;
            return new Rating(response.getData().getAverageRating(), response.getData().getTotalReviews());
        } catch (Exception e) {
            log.warn("booking-service unreachable, showing movie without rating: {}", e.getMessage());
            return Rating.EMPTY;
        }
    }
}
