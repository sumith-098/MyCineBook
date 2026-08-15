package com.cinebook.booking.controller;

import com.cinebook.booking.dto.*;
import com.cinebook.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    private Long custId(Authentication auth) { return (Long) auth.getPrincipal(); }
    private String claim(Authentication auth, String name) {
        return ((io.jsonwebtoken.Claims) auth.getDetails()).get(name, String.class);
    }

    /** Public — lets the seat picker grey out taken seats before the customer logs in. */
    @GetMapping("/booked-seats")
    public ApiResponse<List<String>> bookedSeats(@RequestParam Long showtimeId) {
        return ApiResponse.ok("OK", bookingService.bookedSeats(showtimeId));
    }

    @PostMapping("/internal/quote")
    public ApiResponse<BookingQuoteDto> quote(@Valid @RequestBody QuoteRequest req) {
        return ApiResponse.ok("OK", bookingService.quote(req));
    }
     @PostMapping("/quote")
    public ApiResponse<BookingQuoteDto> quotes(@Valid @RequestBody QuoteRequest req) {
        return ApiResponse.ok("OK", bookingService.quote(req));
    }

    // @PostMapping
    // public ApiResponse<BookingDto> book(Authentication auth, @Valid @RequestBody CreateBookingRequest req) {
    //     BookingDto dto = bookingService.book(custId(auth), claim(auth, "email"), claim(auth, "name"), req);
    //     return ApiResponse.ok("🎉 " + dto.getSeats().size() + " seat(s) booked! Ref: " + dto.getBookingGroup()
    //             + ". Check your email!", dto);
    // }

    @GetMapping("/my")
    public ApiResponse<List<BookingDto>> myBookings(Authentication auth) {
        return ApiResponse.ok("OK", bookingService.myBookings(custId(auth)));
    }

    @GetMapping("/{bookingId}")
    public ApiResponse<BookingDto> detail(Authentication auth, @PathVariable Long bookingId) {
        return ApiResponse.ok("OK", bookingService.bookingDetail(custId(auth), bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public ApiResponse<Map<String, Integer>> cancel(Authentication auth, @PathVariable Long bookingId) {
        int released = bookingService.cancel(custId(auth), bookingId);
        return ApiResponse.ok("Booking cancelled — " + released + " seat(s) released.", Map.of("seatsReleased", released));
    }
}
