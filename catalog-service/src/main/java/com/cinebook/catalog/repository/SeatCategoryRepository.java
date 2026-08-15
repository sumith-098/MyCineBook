package com.cinebook.catalog.repository;

import com.cinebook.catalog.entity.SeatCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatCategoryRepository extends JpaRepository<SeatCategory, Long> {
    List<SeatCategory> findByTheaterIdAndScreenNameOrderBySortOrder(Long theaterId, String screenName);
    List<SeatCategory> findByTheaterId(Long theaterId); // used for price-range calc across all screens
    void deleteByTheaterIdAndScreenName(Long theaterId, String screenName);
}
