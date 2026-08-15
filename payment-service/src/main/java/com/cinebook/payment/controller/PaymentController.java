package com.cinebook.payment.controller;

import com.cinebook.payment.dto.*;
import com.cinebook.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    private Long custId(Authentication auth) { return (Long) auth.getPrincipal(); }
    private String claim(Authentication auth, String name) {
        return ((io.jsonwebtoken.Claims) auth.getDetails()).get(name, String.class);
    }

    @PostMapping("/razorpay/create-order")
    public ApiResponse<CreateOrderResponse> createOrder(Authentication auth, @Valid @RequestBody CreateOrderRequest req) {
        CreateOrderResponse resp = paymentService.createOrder(custId(auth), claim(auth, "email"), req);
        return ApiResponse.ok("OK", resp);
    }

    @PostMapping("/razorpay/verify")
    public ApiResponse<Map<String, Object>> verify(Authentication auth, @Valid @RequestBody VerifyPaymentRequest req) {
        Map<String, Object> result = paymentService.verifyAndBook(custId(auth), claim(auth, "email"), claim(auth, "name"), req);
        return ApiResponse.ok("🎉 Payment successful — booking confirmed!", result);
    }
}
