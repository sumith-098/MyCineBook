package com.cinebook.catalog.dto;

public class AdminTheaterDto {
    private Long theaterId;
    private String theaterName;
    private Long ownerId;

    public AdminTheaterDto() {}
    public AdminTheaterDto(Long theaterId, String theaterName, Long ownerId) {
        this.theaterId = theaterId; this.theaterName = theaterName; this.ownerId = ownerId;
    }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}
