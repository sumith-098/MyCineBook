package com.cinebook.catalog.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ScreenLayoutRequest {
    @NotBlank(message = "Screen name is required")
    private String screenName;

    // { "rows": [ { "label": "A", "seats": 12, "category": "Gold" }, ... ] }
    @NotNull(message = "Layout rows are required")
    private LayoutPayload layout;

    @NotEmpty(message = "At least one seat category is required")
    private List<@Valid SeatCategoryDto> categories;

    private boolean forceSave = false;

    public static class LayoutPayload {
        @NotEmpty(message = "Layout must have at least one row")
        private List<RowDef> rows;
        public List<RowDef> getRows() { return rows; }
        public void setRows(List<RowDef> rows) { this.rows = rows; }
    }

    public static class RowDef {
        @NotBlank
        private String label;
        @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(60)
        private int seats;
        @NotBlank
        private String category;
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public int getSeats() { return seats; }
        public void setSeats(int seats) { this.seats = seats; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public String getScreenName() { return screenName; }
    public void setScreenName(String screenName) { this.screenName = screenName; }
    public LayoutPayload getLayout() { return layout; }
    public void setLayout(LayoutPayload layout) { this.layout = layout; }
    public List<SeatCategoryDto> getCategories() { return categories; }
    public void setCategories(List<SeatCategoryDto> categories) { this.categories = categories; }
    public boolean isForceSave() { return forceSave; }
    public void setForceSave(boolean forceSave) { this.forceSave = forceSave; }
}
