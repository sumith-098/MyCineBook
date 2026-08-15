package com.cinebook.notification.controller;

import com.cinebook.notification.dto.ApiResponse;
import com.cinebook.notification.dto.BookingConfirmationRequest;
import com.cinebook.notification.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/booking-confirmation")
    public ApiResponse<Void> bookingConfirmation(@Valid @RequestBody BookingConfirmationRequest req) {
        boolean sent = emailService.sendBookingConfirmation(req);
        return sent ? ApiResponse.ok("Email sent.") : ApiResponse.error("Email delivery failed.");
    }
}
