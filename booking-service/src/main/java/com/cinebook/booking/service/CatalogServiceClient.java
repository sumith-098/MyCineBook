package com.cinebook.booking.service;

import com.cinebook.booking.dto.ApiResponse;
import com.cinebook.booking.dto.ShowtimeInfoResponse;
import com.cinebook.booking.exception.ApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CatalogServiceClient {

    private final RestClient restClient;

    public CatalogServiceClient(RestClient catalogServiceRestClient) {
        this.restClient = catalogServiceRestClient;
    }

    /** THE source of truth for showtime layout + seat category prices — never trust the client for this. */
    public ShowtimeInfoResponse getShowtimeInfo(Long showtimeId) {
        try {
            ApiResponse<ShowtimeInfoResponse> response = restClient.get()
                    .uri("/api/catalog/showtimes/{id}", showtimeId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<ShowtimeInfoResponse>>() {});
            if (response == null || response.getData() == null || response.getData().getShowtimeId() == null) {
                throw new ApiException("Showtime not found.", HttpStatus.NOT_FOUND);
            }
            return response.getData();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Could not reach catalog service. Please try again.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
