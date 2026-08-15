package com.cinebook.booking.service;

import com.cinebook.booking.dto.BookingDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * notification-service isn't built yet. This client soft-fails (logs and moves on) rather than
 * blocking or rolling back a booking just because the confirmation email couldn't be sent —
 * the booking itself must never depend on email delivery succeeding.
 */
@Service
public class NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationClient.class);

    private final RestClient restClient;

    public NotificationClient(RestClient notificationServiceRestClient) {
        this.restClient = notificationServiceRestClient;
    }

    public void sendBookingConfirmation(String email, String custName, BookingDto booking) {
        try {
            restClient.post()
                    .uri("/api/notifications/booking-confirmation")
                    .body(Map.of(
                            "email", email,
                            "custName", custName,
                            "movieTitle", booking.getMovieTitle(),
                            "theaterName", booking.getTheaterName(),
                            "showDate", booking.getShowDate(),
                            "showTime", booking.getShowTime(),
                            "seats", booking.getSeats(),
                            "totalAmount", booking.getTotalAmount(),
                            "bookingRef", booking.getBookingRef()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("notification-service unreachable ({}) — booking confirmation email not sent for {}. " +
                     "Booking itself succeeded and is unaffected.", e.getMessage(), booking.getBookingRef());
        }
    }
}
