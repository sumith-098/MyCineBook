package com.cinebook.catalog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Cross-service call to booking-service. This check is BEST-EFFORT ("is it worth warning the
 * owner?") — not a data-integrity guarantee. Existing bookings store their own locked-in price
 * at booking time (see booking.amount in booking-service), so a later layout/price change can
 * never silently alter money already charged. If booking-service is unreachable, we log and
 * skip the warning rather than blocking the owner's save.
 */
@Service
public class BookingServiceClient {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceClient.class);

    private final RestClient restClient;

    @org.springframework.beans.factory.annotation.Value("${app.internal.api-key:}")
    private String internalApiKey;

    public BookingServiceClient(RestClient bookingServiceRestClient) {
        this.restClient = bookingServiceRestClient;
    }

    public int countActiveBookings(Long theaterId, String screenName) {
        try {
            Map<?, ?> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/bookings/internal/active-count")
                            .queryParam("theaterId", theaterId)
                            .queryParam("screen", screenName)
                            .build())
                    .header("X-Internal-Api-Key", internalApiKey)
                    .retrieve()
                    .body(Map.class);
            Object count = response == null ? null : response.get("count");
            return count instanceof Number ? ((Number) count).intValue() : 0;
        } catch (Exception e) {
            log.warn("booking-service unreachable, skipping active-bookings warning: {}", e.getMessage());
            return 0;
        }
    }
}
