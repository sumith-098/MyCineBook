package com.cinebook.catalog.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "screen_layout", uniqueConstraints = @UniqueConstraint(columnNames = {"theater_id", "screen_name"}))
public class ScreenLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "layout_id")
    private Long layoutId;

    @Column(name = "theater_id", nullable = false)
    private Long theaterId;

    @Column(name = "screen_name", nullable = false, length = 50)
    private String screenName;

    @Column(name = "row_count")
    private Integer rowCount = 10;

    @Column(name = "total_seat_count")
    private Integer totalSeatCount = 0;

    // Raw JSON blob describing seat rows/labels/categories, same shape the old React-free
    // frontend already produced — the new React seat-map editor will post/consume this as-is.
    @Column(name = "layout_json", columnDefinition = "TEXT")
    private String layoutJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getLayoutId() { return layoutId; }
    public void setLayoutId(Long layoutId) { this.layoutId = layoutId; }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public Integer getRowCount() { return rowCount; }
    public void setRowCount(Integer rowCount) { this.rowCount = rowCount; }
    public Integer getTotalSeatCount() { return totalSeatCount; }
    public void setTotalSeatCount(Integer totalSeatCount) { this.totalSeatCount = totalSeatCount; }
    public String getLayoutJson() { return layoutJson; }
    public void setLayoutJson(String layoutJson) { this.layoutJson = layoutJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
