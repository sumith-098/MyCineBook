package com.cinebook.admin.service;

import com.cinebook.admin.dto.*;
import com.cinebook.admin.exception.ApiException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Every call here forwards the ADMIN'S OWN JWT (not a separate internal service key) to the
 * sibling service's ROLE_ADMIN-protected endpoints. That keeps these reads auditable as actions
 * taken by that specific admin, rather than an anonymous "the platform did this" service call —
 * appropriate here since every one of these is a read triggered directly by an admin's own
 * dashboard visit, not an autonomous background process.
 */
@Service
public class SiblingServiceClient {

    private final RestClient authClient;
    private final RestClient catalogClient;
    private final RestClient bookingClient;

    public SiblingServiceClient(RestClient authServiceRestClient, RestClient catalogServiceRestClient,
                                 RestClient bookingServiceRestClient) {
        this.authClient = authServiceRestClient;
        this.catalogClient = catalogServiceRestClient;
        this.bookingClient = bookingServiceRestClient;
    }

    private <T> T get(RestClient client, String path, String bearerToken, ParameterizedTypeReference<ApiResponse<T>> type) {
        try {
            ApiResponse<T> response = client.get()
                    .uri(path)
                    .header("Authorization", bearerToken)
                    .retrieve()
                    .body(type);
            if (response == null || response.getData() == null) {
                throw new ApiException("Empty response from " + path, HttpStatus.BAD_GATEWAY);
            }
            return response.getData();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Could not reach a dependent service (" + path + "). Please try again.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public AuthStatsLiteDto authStats(String bearerToken) {
        return get(authClient, "/api/auth/admin/stats", bearerToken, new ParameterizedTypeReference<>() {});
    }

    public List<AdminOwnerSummaryDto> pendingOwners(String bearerToken) {
        return get(authClient, "/api/auth/admin/owners/pending", bearerToken, new ParameterizedTypeReference<>() {});
    }

    public List<AdminOwnerSummaryDto> activeOwners(String bearerToken) {
        return get(authClient, "/api/auth/admin/owners/active", bearerToken, new ParameterizedTypeReference<>() {});
    }

    public CatalogStatsLiteDto catalogStats(String bearerToken) {
        return get(catalogClient, "/api/catalog/admin/stats", bearerToken, new ParameterizedTypeReference<>() {});
    }

    public List<AdminTheaterLiteDto> allTheaters(String bearerToken) {
        return get(catalogClient, "/api/catalog/admin/theaters", bearerToken, new ParameterizedTypeReference<>() {});
    }

    public BookingStatsLiteDto bookingStats(String bearerToken) {
        return get(bookingClient, "/api/bookings/admin/stats", bearerToken, new ParameterizedTypeReference<>() {});
    }

    public List<RecentBookingDto> recentBookings(String bearerToken) {
        return get(bookingClient, "/api/bookings/admin/recent", bearerToken, new ParameterizedTypeReference<>() {});
    }

    public List<TheaterEarningLiteDto> earningsByTheater(String bearerToken, List<Long> theaterIds) {
        if (theaterIds.isEmpty()) return List.of();
        String query = theaterIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        return get(bookingClient, "/api/bookings/admin/earnings-by-theater?theaterIds=" + query, bearerToken,
                new ParameterizedTypeReference<>() {});
    }
}
