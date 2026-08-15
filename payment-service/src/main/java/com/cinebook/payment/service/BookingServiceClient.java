package com.cinebook.payment.service;

import com.cinebook.payment.dto.*;
import com.cinebook.payment.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class BookingServiceClient {

    private final RestClient restClient;

    @Value("${app.internal.api-key}")
    private String internalApiKey;

    public BookingServiceClient(RestClient bookingServiceRestClient) {
        this.restClient = bookingServiceRestClient;
    }

    /** THE trusted amount — never accept a price/amount from the client directly. */
    public BookingQuoteResponse getQuote(Long movieId, Long showtimeId, List<String> seats) {
        try {
            ApiResponse<BookingQuoteResponse> response = restClient.post()
                    .uri("/api/bookings/internal/quote")
                    .header("X-Internal-Api-Key", internalApiKey) 
                    .body(java.util.Map.of("movieId", movieId, "showtimeId", showtimeId, "seats", seats))
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<BookingQuoteResponse>>() {});
            if (response == null || response.getData() == null) {
                throw new ApiException("Could not price the selected seats.", HttpStatus.BAD_GATEWAY);
            }
            return response.getData();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Could not reach booking service. Please try again.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /** Called only after a verified payment. Uses the internal API key, not the customer's JWT,
     *  since this is a service-to-service call and booking-service must trust ONLY this key —
     *  never a client-supplied "I already paid" flag. */
    public ConfirmedBookingResponse confirmPaidBooking(Long custId, String custEmail, String custName,
                                                        Long movieId, Long showtimeId, List<String> seats,
                                                        String paymentReference) {
        ApiResponse<ConfirmedBookingResponse> response = restClient.post()
                .uri("/api/bookings/internal/confirm-paid")
                .header("X-Internal-Api-Key", internalApiKey)
                .body(java.util.Map.of(
                        "custId", custId, "custEmail", custEmail, "custName", custName == null ? "" : custName,
                        "movieId", movieId, "showtimeId", showtimeId, "seats", seats, "paymentReference", paymentReference
                ))
                .retrieve()
                .onStatus(status -> status.value() == 409, (req, res) -> {
                    throw new ApiException("SEAT_CONFLICT", HttpStatus.CONFLICT);
                })
                .body(new ParameterizedTypeReference<ApiResponse<ConfirmedBookingResponse>>() {});
        if (response == null || response.getData() == null) {
            throw new ApiException("Booking service did not confirm the booking.", HttpStatus.BAD_GATEWAY);
        }
        return response.getData();
    }
}
