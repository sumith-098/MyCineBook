package com.cinebook.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AddShowtimeRequest {
    @NotNull(message = "Show date is required")
    private String showDate; // yyyy-MM-dd

    @NotNull(message = "Show time is required")
    private String showTime; // HH:mm

    @NotBlank(message = "Screen is required")
    private String screen;

    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }
}
