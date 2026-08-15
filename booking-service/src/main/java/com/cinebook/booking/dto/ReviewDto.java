package com.cinebook.booking.dto;

public class ReviewDto {
    private Long reviewId;
    private String custName;
    private Integer rating;
    private String reviewText;
    private String createdAt;

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }
    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
