package com.cinebook.payment.service;

import com.cinebook.payment.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.Map;

/**
 * Talks to api.razorpay.com directly (Basic Auth: keyId:keySecret) instead of pulling in the
 * razorpay-java SDK — keeps this small and every outbound call auditable in one place.
 */
@Service
public class RazorpayClient {

    private static final Logger log = LoggerFactory.getLogger(RazorpayClient.class);

    private final RestClient restClient;

    public RazorpayClient(RestClient razorpayRestClient) {
        this.restClient = razorpayRestClient;
    }

    private String basicAuth(String keyId, String keySecret) {
        return "Basic " + Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes());
    }

    @SuppressWarnings("unchecked")
    public String createOrder(String keyId, String keySecret, long amountPaise, Map<String, Object> notes) {
        try {
            Map<String, Object> body = Map.of(
                    "amount", amountPaise,
                    "currency", "INR",
                    "notes", notes
            );
            Map<String, Object> response = restClient.post()
                    .uri("/orders")
                    .header("Authorization", basicAuth(keyId, keySecret))
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            if (response == null || response.get("id") == null) {
                throw new ApiException("Razorpay did not return an order id.", HttpStatus.BAD_GATEWAY);
            }
            return (String) response.get("id");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Razorpay create-order failed: {}", e.getMessage());
            throw new ApiException("Could not reach the payment gateway. Please try again.", HttpStatus.BAD_GATEWAY);
        }
    }

    @SuppressWarnings("unchecked")
    public String refund(String keyId, String keySecret, String paymentId, long amountPaise, String reason) {
        try {
            Map<String, Object> body = Map.of(
                    "amount", amountPaise,
                    "notes", Map.of("reason", reason)
            );
            Map<String, Object> response = restClient.post()
                    .uri("/payments/{paymentId}/refund", paymentId)
                    .header("Authorization", basicAuth(keyId, keySecret))
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return response == null ? null : (String) response.get("id");
        } catch (Exception e) {
            log.error("Razorpay refund FAILED for payment {}: {} — needs manual follow-up!", paymentId, e.getMessage());
            return null; // caller must handle: refund failed, customer needs manual support
        }
    }
}
