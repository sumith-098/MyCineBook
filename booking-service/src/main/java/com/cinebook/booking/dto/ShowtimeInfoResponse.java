package com.cinebook.booking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShowtimeInfoResponse {
    private Long showtimeId;
    private Long movieId;
    private String movieTitle;
    private String showDate;
    private String showTime;
    private String screen;
    private Long theaterId;
    private String theaterName;
    private String location;
    private Integer totalSeats;
    private LayoutPayload layout;
    private Map<String, CatPrice> catPrices;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LayoutPayload {
        private java.util.List<RowDef> rows;
        public java.util.List<RowDef> getRows() { return rows; }
        public void setRows(java.util.List<RowDef> rows) { this.rows = rows; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RowDef {
        private String label;
        private int seats;
        private String category;
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public int getSeats() { return seats; }
        public void setSeats(int seats) { this.seats = seats; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CatPrice {
        private BigDecimal price;
        private String color;
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    public LayoutPayload getLayout() { return layout; }
    public void setLayout(LayoutPayload layout) { this.layout = layout; }
    public Map<String, CatPrice> getCatPrices() { return catPrices; }
    public void setCatPrices(Map<String, CatPrice> catPrices) { this.catPrices = catPrices; }
}