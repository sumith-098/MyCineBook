package com.cinebook.catalog.service;

import com.cinebook.catalog.dto.ScreenLayoutRequest;
import com.cinebook.catalog.dto.ScreenLayoutResponseDto;
import com.cinebook.catalog.dto.SeatCategoryDto;
import com.cinebook.catalog.entity.ScreenLayout;
import com.cinebook.catalog.entity.SeatCategory;
import com.cinebook.catalog.entity.Theater;
import com.cinebook.catalog.exception.ApiException;
import com.cinebook.catalog.repository.ScreenLayoutRepository;
import com.cinebook.catalog.repository.SeatCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScreenLayoutService {

    private final ScreenLayoutRepository screenLayoutRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final TheaterService theaterService;
    private final BookingServiceClient bookingServiceClient;
    private final ObjectMapper objectMapper;

    public ScreenLayoutService(ScreenLayoutRepository screenLayoutRepository, SeatCategoryRepository seatCategoryRepository,
                                TheaterService theaterService, BookingServiceClient bookingServiceClient,
                                ObjectMapper objectMapper) {
        this.screenLayoutRepository = screenLayoutRepository;
        this.seatCategoryRepository = seatCategoryRepository;
        this.theaterService = theaterService;
        this.bookingServiceClient = bookingServiceClient;
        this.objectMapper = objectMapper;
    }

    public List<ScreenLayoutResponseDto> listForOwner(Long ownerId, Long theaterId) {
        theaterService.getOwnedTheaterOrThrow(ownerId, theaterId); // 403s if not the owner
        return screenLayoutRepository.findByTheaterId(theaterId).stream().map(this::toDto).toList();
    }

    /** Returns activeBookingCount so the frontend can show the same "click save again to confirm" warning. */
    public record SaveResult(ScreenLayoutResponseDto layout, int activeBookingCount, boolean saved) {}

    @Transactional
    public SaveResult save(Long ownerId, Long theaterId, ScreenLayoutRequest req) {
        Theater theater = theaterService.getOwnedTheaterOrThrow(ownerId, theaterId);

        // Every seat's category must exist in the submitted categories list, or seats end up
        // with a price-less category and booking-service would have nothing to charge for them.
        Set<String> categoryNames = req.getCategories().stream().map(SeatCategoryDto::getCategory).collect(Collectors.toSet());
        for (var row : req.getLayout().getRows()) {
            if (!categoryNames.contains(row.getCategory())) {
                throw new ApiException("Row '" + row.getLabel() + "' uses category '" + row.getCategory()
                        + "' which isn't in the categories list.", HttpStatus.BAD_REQUEST);
            }
        }

        if (!req.isForceSave()) {
            int activeCount = bookingServiceClient.countActiveBookings(theaterId, req.getScreenName());
            if (activeCount > 0) {
                // Don't save yet — let the owner see the warning and resubmit with forceSave=true.
                // (Existing bookings already have their price locked in booking.amount, so this
                // is a heads-up, not a financial-integrity risk — same UX as the original app.)
                return new SaveResult(null, activeCount, false);
            }
        }

        int totalSeats = req.getLayout().getRows().stream().mapToInt(ScreenLayoutRequest.RowDef::getSeats).sum();
        String layoutJson;
        try {
            layoutJson = objectMapper.writeValueAsString(req.getLayout());
        } catch (Exception e) {
            throw new ApiException("Could not serialize layout.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        ScreenLayout layout = screenLayoutRepository.findByTheaterIdAndScreenName(theaterId, req.getScreenName())
                .orElseGet(ScreenLayout::new);
        layout.setTheaterId(theaterId);
        layout.setScreenName(req.getScreenName().trim());
        layout.setRowCount(req.getLayout().getRows().size());
        layout.setTotalSeatCount(totalSeats);
        layout.setLayoutJson(layoutJson);
        layout.setUpdatedAt(LocalDateTime.now());
        screenLayoutRepository.save(layout);

        seatCategoryRepository.deleteByTheaterIdAndScreenName(theaterId, req.getScreenName());
        for (SeatCategoryDto cat : req.getCategories()) {
            SeatCategory entity = new SeatCategory();
            entity.setTheaterId(theaterId);
            entity.setScreenName(req.getScreenName().trim());
            entity.setCategory(cat.getCategory().trim());
            entity.setPrice(cat.getPrice());
            entity.setSortOrder(cat.getSortOrder() == null ? 0 : cat.getSortOrder());
            entity.setColorCode(cat.getColor() == null || cat.getColor().isBlank() ? "#4a5568" : cat.getColor());
            seatCategoryRepository.save(entity);
        }

        return new SaveResult(toDto(layout), 0, true);
    }

    private ScreenLayoutResponseDto toDto(ScreenLayout layout) {
        ScreenLayoutResponseDto dto = new ScreenLayoutResponseDto();
        dto.setLayoutId(layout.getLayoutId());
        dto.setScreenName(layout.getScreenName());
        dto.setTotalSeatCount(layout.getTotalSeatCount());
        try {
            dto.setLayout(layout.getLayoutJson() == null ? null : objectMapper.readValue(layout.getLayoutJson(), Object.class));
        } catch (Exception e) {
            dto.setLayout(null);
        }
        List<SeatCategory> cats = seatCategoryRepository.findByTheaterIdAndScreenNameOrderBySortOrder(
                layout.getTheaterId(), layout.getScreenName());
        dto.setCategories(cats.stream().map(c -> {
            SeatCategoryDto d = new SeatCategoryDto();
            d.setCategory(c.getCategory());
            d.setPrice(c.getPrice());
            d.setSortOrder(c.getSortOrder());
            d.setColor(c.getColorCode());
            return d;
        }).toList());
        return dto;
    }
}
