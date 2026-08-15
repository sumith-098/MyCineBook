package com.cinebook.catalog.repository;

import com.cinebook.catalog.entity.ScreenLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ScreenLayoutRepository extends JpaRepository<ScreenLayout, Long> {
    List<ScreenLayout> findByTheaterId(Long theaterId);
    Optional<ScreenLayout> findByTheaterIdAndScreenName(Long theaterId, String screenName);
}
