package com.cinebook.booking.controller;

import com.cinebook.booking.dto.ApiResponse;
import com.cinebook.booking.dto.BookingDto;
import com.cinebook.booking.dto.ConfirmPaidBookingRequest;
import com.cinebook.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Service-to-service only — guarded by InternalApiKeyFilter, not end-user JWTs. */
@RestController
@RequestMapping("/api/bookings/internal")
public class InternalBookingController {

    private final BookingService bookingService;

    public InternalBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/active-count")
    public ApiResponse<Map<String, Long>> activeCount(@RequestParam Long theaterId, @RequestParam String screen) {
        long count = bookingService.countActiveBookings(theaterId, screen);
        return ApiResponse.ok("OK", Map.of("count", count));
    }

    /** Called only by payment-service, only after independently verifying the Razorpay signature. */
    @PostMapping("/confirm-paid")
    public ApiResponse<BookingDto> confirmPaid(@Valid @RequestBody ConfirmPaidBookingRequest req) {
        BookingDto dto = bookingService.confirmPaidBooking(req.getCustId(), req.getCustEmail(), req.getCustName(),
                req.getMovieId(), req.getShowtimeId(), req.getSeats(), req.getPaymentReference());
        return ApiResponse.ok("Booking confirmed.", dto);
    }
}

