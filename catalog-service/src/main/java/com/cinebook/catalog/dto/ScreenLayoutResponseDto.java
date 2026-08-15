package com.cinebook.catalog.dto;

import java.util.List;

public class ScreenLayoutResponseDto {
    private Long layoutId;
    private String screenName;
    private Object layout; // parsed layout_json
    private List<SeatCategoryDto> categories;
    private Integer totalSeatCount;

    public Long getLayoutId() { return layoutId; }
    public void setLayoutId(Long layoutId) { this.layoutId = layoutId; }
    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public Object getLayout() { return layout; }
    public void setLayout(Object layout) { this.layout = layout; }
    public List<SeatCategoryDto> getCategories() { return categories; }
    public void setCategories(List<SeatCategoryDto> categories) { this.categories = categories; }
    public Integer getTotalSeatCount() { return totalSeatCount; }
    public void setTotalSeatCount(Integer totalSeatCount) { this.totalSeatCount = totalSeatCount; }
}
