package com.cinebook.booking.controller;

import com.cinebook.booking.dto.*;
import com.cinebook.booking.entity.Booking;
import com.cinebook.booking.repository.BookingRepository;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** ROLE_ADMIN only (see SecurityConfig — placed ABOVE the general /api/bookings/** customer
 *  rule so it isn't shadowed by it). Called by admin-service, forwarding the admin's own JWT. */
@RestController
@RequestMapping("/api/bookings/admin")
public class AdminBookingController {

    private final BookingRepository bookingRepository;

    public AdminBookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/stats")
    public ApiResponse<AdminBookingStatsDto> stats() {
        return ApiResponse.ok("OK", new AdminBookingStatsDto(bookingRepository.countActiveOrWatched(), bookingRepository.sumRevenue()));
    }

    @GetMapping("/recent")
    public ApiResponse<List<AdminRecentBookingDto>> recent() {
        List<AdminRecentBookingDto> dtos = bookingRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::toRecentDto).toList();
        return ApiResponse.ok("OK", dtos);
    }

    /** Earnings per theater, razorpay-paid confirmed/watched bookings only — used by
     *  admin-service to compute owner settlements (theater -> owner mapping lives in
     *  catalog-service, so admin-service combines the two). */
    @GetMapping("/earnings-by-theater")
    public ApiResponse<List<TheaterEarningDto>> earningsByTheater(@RequestParam List<Long> theaterIds) {
        List<Object[]> rows = bookingRepository.sumEarningsByTheaterIds(theaterIds);
        List<TheaterEarningDto> result = rows.stream()
                .map(r -> new TheaterEarningDto((Long) r[0], (BigDecimal) r[1]))
                .toList();
        return ApiResponse.ok("OK", result);
    }

    private AdminRecentBookingDto toRecentDto(Booking b) {
        AdminRecentBookingDto dto = new AdminRecentBookingDto();
        dto.setBookingRef(b.getBookingRef());
        dto.setMovieTitle(b.getMovieTitle());
        dto.setTheaterName(b.getTheaterName());
        dto.setAmount(b.getAmount());
        dto.setStatus(b.getStatus().name());
        dto.setPaymentMethod(b.getPaymentMethod());
        dto.setCreatedAt(b.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return dto;
    }
}
