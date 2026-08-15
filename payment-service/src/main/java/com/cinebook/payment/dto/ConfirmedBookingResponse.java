package com.cinebook.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Mirrors booking-service's BookingDto — only the fields payment-service needs to report back. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmedBookingResponse {
    private String bookingGroup;
    private String bookingRef;

    public String getBookingGroup() { return bookingGroup; }
    public void setBookingGroup(String bookingGroup) { this.bookingGroup = bookingGroup; }
    public String getBookingRef() { return bookingRef; }
    public void setBookingRef(String bookingRef) { this.bookingRef = bookingRef; }
}
