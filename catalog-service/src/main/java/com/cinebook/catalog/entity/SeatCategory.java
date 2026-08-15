package com.cinebook.catalog.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "seat_category")
public class SeatCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cat_id")
    private Long catId;

    @Column(name = "theater_id", nullable = false)
    private Long theaterId;

    @Column(name = "screen_name", nullable = false, length = 50)
    private String screenName;

    @Column(nullable = false, length = 50)
    private String category;

    // This is the ONLY source of truth for seat price. booking-service and payment-service
    // must always re-read this server-side and never trust a price submitted by the client.
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "color_code", length = 20)
    private String colorCode = "#4a5568";

    public Long getCatId() { return catId; }
    public void setCatId(Long catId) { this.catId = catId; }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
}
