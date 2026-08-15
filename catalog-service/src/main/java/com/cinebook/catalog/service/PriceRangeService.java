package com.cinebook.catalog.service;

import com.cinebook.catalog.entity.SeatCategory;
import com.cinebook.catalog.repository.SeatCategoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PriceRangeService {

    private final SeatCategoryRepository seatCategoryRepository;

    public PriceRangeService(SeatCategoryRepository seatCategoryRepository) {
        this.seatCategoryRepository = seatCategoryRepository;
    }

    public record PriceRange(BigDecimal min, BigDecimal max) {}

    public PriceRange rangeForTheater(Long theaterId) {
        List<SeatCategory> cats = seatCategoryRepository.findByTheaterId(theaterId);
        if (cats.isEmpty()) return new PriceRange(null, null);
        BigDecimal min = cats.stream().map(SeatCategory::getPrice).min(BigDecimal::compareTo).orElse(null);
        BigDecimal max = cats.stream().map(SeatCategory::getPrice).max(BigDecimal::compareTo).orElse(null);
        return new PriceRange(min, max);
    }
}
