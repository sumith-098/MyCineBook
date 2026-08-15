package com.cinebook.booking.service;

import com.cinebook.booking.dto.*;
import com.cinebook.booking.entity.Booking;
import com.cinebook.booking.entity.BookingStatus;
import com.cinebook.booking.entity.SeatLock;
import com.cinebook.booking.exception.ApiException;
import com.cinebook.booking.exception.SeatConflictException;
import com.cinebook.booking.repository.BookingRepository;
import com.cinebook.booking.repository.ReviewRepository;
import com.cinebook.booking.repository.SeatLockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final BookingRepository bookingRepository;
    private final SeatLockRepository seatLockRepository;
    private final ReviewRepository reviewRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final SeatPricingService seatPricingService;
    private final NotificationClient notificationClient;

    @Value("${app.cancellation.min-hours-before-showtime:2}")
    private int minCancelHours;

    public BookingService(BookingRepository bookingRepository, SeatLockRepository seatLockRepository,
                           ReviewRepository reviewRepository, CatalogServiceClient catalogServiceClient,
                           SeatPricingService seatPricingService, NotificationClient notificationClient) {
        this.bookingRepository = bookingRepository;
        this.seatLockRepository = seatLockRepository;
        this.reviewRepository = reviewRepository;
        this.catalogServiceClient = catalogServiceClient;
        this.seatPricingService = seatPricingService;
        this.notificationClient = notificationClient;
    }

    // ── Step 1: quote (no DB writes, no lock — just pricing for the confirmation screen) ──
    public BookingQuoteDto quote(QuoteRequest req) {
        ShowtimeInfoResponse info = catalogServiceClient.getShowtimeInfo(req.getShowtimeId());
        assertUpcoming(info);

        List<SeatQuoteDto> priced = seatPricingService.priceSeats(info, dedupe(req.getSeats()));
        BigDecimal total = priced.stream().map(SeatQuoteDto::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        BookingQuoteDto dto = new BookingQuoteDto();
        dto.setMovieId(info.getMovieId());
        dto.setMovieTitle(info.getMovieTitle());
        dto.setShowtimeId(info.getShowtimeId());
        dto.setShowDate(info.getShowDate());
        dto.setShowTime(info.getShowTime());
        dto.setScreen(info.getScreen());
        dto.setTheaterName(info.getTheaterName());
        dto.setLocation(info.getLocation());
        dto.setSeatDetails(priced);
        dto.setTotalAmount(total);
        return dto;
    }

    // ── Step 2: create the booking — concurrency-safe seat locking ──────────
    // NOTE: the only way to create a booking is now confirmPaidBooking() below, called by
    // payment-service AFTER it independently verifies a Razorpay signature. There is no
    // "pay at counter"/cash path anymore — every booking on this platform requires a completed
    // online payment first. (There used to be a public cash-booking entrypoint here; it was
    // removed because a "book now, pay when you arrive" flow isn't really an online booking at
    // all — the seat was never actually secured by anything until the customer showed up.)

    /**
     * Called ONLY by payment-service (via InternalBookingController, guarded by
     * ROLE_INTERNAL_SERVICE) AFTER it has independently verified the Razorpay signature
     * server-side. This is the one path allowed to create a booking with paymentMethod=razorpay.
     * If seat locking fails here (someone else grabbed the seat between quote and payment
     * completing), the caller (payment-service) is responsible for refunding — see its
     * verifyAndBook() — this method just throws SeatConflictException same as before.
     */
    @Transactional
    public BookingDto confirmPaidBooking(Long custId, String custEmail, String custName, Long movieId,
                                          Long showtimeId, List<String> seats, String paymentReference) {
        return createBookingInternal(custId, custEmail, custName, movieId, showtimeId, dedupe(seats), "razorpay", paymentReference);
    }

    private BookingDto createBookingInternal(Long custId, String custEmail, String custName, Long movieId,
                                               Long showtimeId, List<String> seats, String paymentMethod,
                                               String paymentReference) {
        ShowtimeInfoResponse info = catalogServiceClient.getShowtimeInfo(showtimeId);
        assertUpcoming(info);
        List<SeatQuoteDto> priced = seatPricingService.priceSeats(info, seats);

        String bookingGroup = generateBookingGroup();
        List<Booking> saved = new ArrayList<>();

        for (SeatQuoteDto sd : priced) {
            Booking booking = new Booking();
            booking.setCustId(custId);
            booking.setMovieId(info.getMovieId());
            booking.setMovieTitle(info.getMovieTitle());
            booking.setTheaterId(info.getTheaterId());
            booking.setTheaterName(info.getTheaterName());
            booking.setLocation(info.getLocation());
            booking.setShowtimeId(info.getShowtimeId());
            booking.setShowDate(LocalDate.parse(info.getShowDate()));
            booking.setShowTime(java.time.LocalTime.parse(info.getShowTime()));
            booking.setScreen(info.getScreen());
            booking.setSeatNo(sd.getSeat());
            booking.setSeatCategory(sd.getCategory());
            booking.setAmount(sd.getPrice());
            booking.setBookingRef(bookingGroup + "_" + sd.getSeat());
            booking.setBookingGroup(bookingGroup);
            booking.setPaymentMethod(paymentMethod);
            booking.setPaymentReference(paymentReference);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking = bookingRepository.saveAndFlush(booking);

            try {
                seatLockRepository.saveAndFlush(new SeatLock(info.getShowtimeId(), sd.getSeat(), booking.getBookingId(), bookingGroup));
            } catch (DataIntegrityViolationException e) {
                log.info("Seat conflict on showtime {} seat {} — rolling back whole booking group", info.getShowtimeId(), sd.getSeat());
                throw new SeatConflictException(sd.getSeat());
            }
            saved.add(booking);
        }

        BookingDto dto = toGroupDto(saved);
        notificationClient.sendBookingConfirmation(custEmail, custName, dto);
        return dto;
    }

    // ── reads ─────────────────────────────────────────────────────────────

    @Transactional
    public List<BookingDto> myBookings(Long custId) {
        autoMarkWatched(custId);
        List<Booking> rows = bookingRepository.findByCustIdOrderByCreatedAtDesc(custId);
        Set<Long> reviewed = reviewRepository.findReviewedBookingIdsByCustId(custId);

        Map<String, List<Booking>> grouped = new LinkedHashMap<>();
        for (Booking b : rows) {
            grouped.computeIfAbsent(b.getBookingGroup(), k -> new ArrayList<>()).add(b);
        }
        return grouped.values().stream()
                .map(group -> {
                    BookingDto dto = toGroupDto(group);
                    dto.setHasReview(group.stream().anyMatch(b -> reviewed.contains(b.getBookingId())));
                    return dto;
                })
                .sorted(Comparator.comparing(BookingDto::getCreatedAt).reversed())
                .toList();
    }

    public BookingDto bookingDetail(Long custId, Long bookingId) {
        Booking booking = bookingRepository.findByBookingIdAndCustId(bookingId, custId)
                .orElseThrow(() -> new ApiException("Booking not found.", HttpStatus.NOT_FOUND));
        List<Booking> group = bookingRepository.findByBookingGroupAndCustId(booking.getBookingGroup(), custId);
        return toGroupDto(group);
    }

    // ── cancellation ──────────────────────────────────────────────────────

    @Transactional
    public int cancel(Long custId, Long bookingId) {
        Booking booking = bookingRepository.findByBookingIdAndCustId(bookingId, custId)
                .orElseThrow(() -> new ApiException("Booking not found.", HttpStatus.NOT_FOUND));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException("Already cancelled.", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime showDateTime = LocalDateTime.of(booking.getShowDate(), booking.getShowTime());
        if (LocalDateTime.now().isAfter(showDateTime.minusHours(minCancelHours))) {
            throw new ApiException("Cannot cancel within " + minCancelHours + " hours of showtime.", HttpStatus.BAD_REQUEST);
        }

        List<Booking> group = bookingRepository.findByBookingGroupAndCustId(booking.getBookingGroup(), custId);
        int released = 0;
        for (Booking b : group) {
            if (b.getStatus() != BookingStatus.CANCELLED) {
                b.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(b);
                released++;
            }
        }
        seatLockRepository.deleteByBookingGroup(booking.getBookingGroup());
        return released;
    }

    // ── internal (called by catalog-service before a screen-layout edit) ───

    public long countActiveBookings(Long theaterId, String screen) {
        return bookingRepository.countByTheaterIdAndScreenAndStatus(theaterId, screen, BookingStatus.CONFIRMED);
    }

    public List<String> bookedSeats(Long showtimeId) {
        return bookingRepository.findByShowtimeIdAndStatusNot(showtimeId, BookingStatus.CANCELLED)
                .stream().map(Booking::getSeatNo).sorted().toList();
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private void assertUpcoming(ShowtimeInfoResponse info) {
        LocalDateTime showDateTime = LocalDateTime.of(LocalDate.parse(info.getShowDate()), java.time.LocalTime.parse(info.getShowTime()));
        if (showDateTime.isBefore(LocalDateTime.now())) {
            throw new ApiException("This showtime has already started or ended.", HttpStatus.BAD_REQUEST);
        }
    }

    private void autoMarkWatched(Long custId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> confirmed = bookingRepository.findByCustIdOrderByCreatedAtDesc(custId).stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .filter(b -> LocalDateTime.of(b.getShowDate(), b.getShowTime()).isBefore(now))
                .toList();
        for (Booking b : confirmed) {
            b.setStatus(BookingStatus.WATCHED);
            bookingRepository.save(b);
        }
    }

    private List<String> dedupe(List<String> seats) {
        return seats.stream().map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }

    private String generateBookingGroup() {
        StringBuilder sb = new StringBuilder("CB");
        for (int i = 0; i < 8; i++) sb.append(RANDOM.nextInt(10));
        return sb.toString();
    }

    private BookingDto toGroupDto(List<Booking> group) {
        Booking first = group.get(0);
        BookingDto dto = new BookingDto();
        dto.setBookingId(first.getBookingId());
        dto.setBookingGroup(first.getBookingGroup());
        dto.setBookingRef(first.getBookingGroup());
        dto.setMovieId(first.getMovieId());
        dto.setMovieTitle(first.getMovieTitle());
        dto.setTheaterName(first.getTheaterName());
        dto.setLocation(first.getLocation());
        dto.setShowDate(first.getShowDate().toString());
        dto.setShowTime(first.getShowTime().toString());
        dto.setScreen(first.getScreen());
        dto.setSeats(group.stream().map(Booking::getSeatNo).sorted().toList());
        dto.setTotalAmount(group.stream().map(Booking::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));

        boolean anyConfirmed = group.stream().anyMatch(b -> b.getStatus() == BookingStatus.CONFIRMED);
        boolean anyWatched = group.stream().anyMatch(b -> b.getStatus() == BookingStatus.WATCHED);
        dto.setStatus(anyConfirmed ? "CONFIRMED" : anyWatched ? "WATCHED" : "CANCELLED");
        dto.setCreatedAt(first.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return dto;
    }
}