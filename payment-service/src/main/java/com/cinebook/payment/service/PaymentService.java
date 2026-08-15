package com.cinebook.payment.service;

import com.cinebook.payment.dto.*;
import com.cinebook.payment.entity.PaymentOrder;
import com.cinebook.payment.entity.PaymentStatus;
import com.cinebook.payment.exception.ApiException;
import com.cinebook.payment.repository.PaymentOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentOrderRepository paymentOrderRepository;
    private final RazorpayConfigService configService;
    private final RazorpayClient razorpayClient;
    private final BookingServiceClient bookingServiceClient;

    public PaymentService(PaymentOrderRepository paymentOrderRepository, RazorpayConfigService configService,
                           RazorpayClient razorpayClient, BookingServiceClient bookingServiceClient) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.configService = configService;
        this.razorpayClient = razorpayClient;
        this.bookingServiceClient = bookingServiceClient;
    }

    @Transactional
    public CreateOrderResponse createOrder(Long custId, String custEmail, CreateOrderRequest req) {
        var creds = configService.getCredentialsOrThrow();

        // THE fix for the original app's vulnerability: amount is derived server-side from
        // booking-service's quote, never accepted from the client.
        BookingQuoteResponse quote = bookingServiceClient.getQuote(req.getMovieId(), req.getShowtimeId(), req.getSeats());
        long amountPaise = quote.getTotalAmount().movePointRight(2).longValueExact();
        if (amountPaise <= 0) {
            throw new ApiException("Invalid amount.", HttpStatus.BAD_REQUEST);
        }

        String razorpayOrderId = razorpayClient.createOrder(creds.keyId(), creds.keySecret(), amountPaise, Map.of(
                "customer_id", custId,
                "movie_id", req.getMovieId(),
                "showtime_id", req.getShowtimeId(),
                "seats", String.join(",", req.getSeats())
        ));

        PaymentOrder order = new PaymentOrder();
        order.setRazorpayOrderId(razorpayOrderId);
        order.setCustId(custId);
        order.setCustEmail(custEmail);
        order.setMovieId(req.getMovieId());
        order.setShowtimeId(req.getShowtimeId());
        order.setSeats(String.join(",", req.getSeats()));
        order.setAmountPaise(amountPaise);
        order.setStatus(PaymentStatus.CREATED);
        paymentOrderRepository.save(order);

        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(razorpayOrderId);
        response.setAmountPaise(amountPaise);
        response.setKeyId(creds.keyId());
        response.setEmail(custEmail);
        return response;
    }

    @Transactional
    public Map<String, Object> verifyAndBook(Long custId, String custEmail, String custName, VerifyPaymentRequest req) {
        PaymentOrder order = paymentOrderRepository.findByRazorpayOrderIdAndCustId(req.getRazorpayOrderId(), custId)
                .orElseThrow(() -> new ApiException("Order not found.", HttpStatus.NOT_FOUND));

        if (order.getStatus() != PaymentStatus.CREATED) {
            // Prevents replaying the same verify call twice (double-booking / double-processing).
            throw new ApiException("This order has already been processed.", HttpStatus.CONFLICT);
        }

        var creds = configService.getCredentialsOrThrow();
        if (!signatureValid(req.getRazorpayOrderId(), req.getRazorpayPaymentId(), req.getRazorpaySignature(), creds.keySecret())) {
            order.setStatus(PaymentStatus.FAILED);
            order.setFailureReason("Invalid signature");
            order.setUpdatedAt(LocalDateTime.now());
            paymentOrderRepository.save(order);
            throw new ApiException("Invalid payment signature.", HttpStatus.BAD_REQUEST);
        }

        order.setRazorpayPaymentId(req.getRazorpayPaymentId());
        order.setStatus(PaymentStatus.PAID);
        order.setUpdatedAt(LocalDateTime.now());
        paymentOrderRepository.save(order);

        List<String> seats = List.of(order.getSeats().split(","));
        try {
            ConfirmedBookingResponse booking = bookingServiceClient.confirmPaidBooking(
                    custId, custEmail, custName, order.getMovieId(), order.getShowtimeId(), seats, req.getRazorpayPaymentId());

            order.setBookingGroup(booking.getBookingGroup());
            order.setUpdatedAt(LocalDateTime.now());
            paymentOrderRepository.save(order);

            return Map.of("bookingGroup", booking.getBookingGroup(), "bookingRef", booking.getBookingRef());

        } catch (Exception bookingError) {
            // Payment succeeded but booking failed (most commonly: someone else grabbed a seat
            // between quote and payment completing) — the customer must never be left having
            // paid for nothing. Auto-refund, same behavior as the original app.
            log.warn("Booking failed after successful payment for order {} — refunding. Reason: {}",
                    order.getRazorpayOrderId(), bookingError.getMessage());

            String refundId = razorpayClient.refund(creds.keyId(), creds.keySecret(), order.getRazorpayPaymentId(),
                    order.getAmountPaise(), "Booking could not be completed: " + bookingError.getMessage());

            order.setStatus(PaymentStatus.REFUNDED);
            order.setRefundId(refundId);
            order.setFailureReason(bookingError.getMessage());
            order.setUpdatedAt(LocalDateTime.now());
            paymentOrderRepository.save(order);

            if (refundId != null) {
                throw new ApiException("Sorry — your seat was just taken by another customer. " +
                        "Your payment has been automatically refunded (refund ID: " + refundId + ").", HttpStatus.CONFLICT);
            } else {
                // Refund call itself failed — this needs a human. Never hide this from the customer.
                throw new ApiException("Your seat was just taken and the automatic refund could not be processed. " +
                        "Please contact support with payment ID " + order.getRazorpayPaymentId() + " — you will be refunded.",
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    /** Constant-time comparison — a naive != or .equals() on the signature leaks timing
     *  information that could theoretically help an attacker guess a valid signature byte by byte. */
    private boolean signatureValid(String orderId, String paymentId, String providedSignature, String keySecret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            String expectedHex = HexFormat.of().formatHex(computed);
            return MessageDigest.isEqual(
                    expectedHex.getBytes(StandardCharsets.UTF_8),
                    providedSignature.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }
}
