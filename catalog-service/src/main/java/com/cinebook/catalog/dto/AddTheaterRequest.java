package com.cinebook.catalog.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AddTheaterRequest {
    @NotBlank(message = "Theater name is required")
    @Size(max = 100)
    private String theaterName;

    @NotBlank(message = "Location is required")
    @Size(max = 150)
    private String location;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Total seats must be at least 1")
    @Max(value = 5000, message = "Total seats looks unrealistic")
    private Integer totalSeats;

    @Pattern(regexp = "^[+0-9 ()-]{6,20}$", message = "Invalid phone number")
    private String phone;

    @Size(max = 100)
    private String city;

    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private BigDecimal longitude;

    public String getTheaterName() { return theaterName; }
    public void setTheaterName(String theaterName) { this.theaterName = theaterName; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
}
