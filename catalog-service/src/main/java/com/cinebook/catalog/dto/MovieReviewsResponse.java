package com.cinebook.catalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Mirrors booking-service's MovieReviewsDto — catalog-service only needs the aggregate numbers. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieReviewsResponse {
    private Double averageRating;
    private Long totalReviews;

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Long getTotalReviews() { return totalReviews; }
    public void setTotalReviews(Long totalReviews) { this.totalReviews = totalReviews; }
}
