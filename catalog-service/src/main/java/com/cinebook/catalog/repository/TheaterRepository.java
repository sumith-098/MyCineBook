package com.cinebook.catalog.repository;

import com.cinebook.catalog.entity.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TheaterRepository extends JpaRepository<Theater, Long> {
    List<Theater> findByIsActiveTrue();
    List<Theater> findByOwnerId(Long ownerId);
    Optional<Theater> findByTheaterIdAndOwnerId(Long theaterId, Long ownerId);
    List<Theater> findByIsActiveTrueAndLatitudeIsNotNullAndLongitudeIsNotNull();
}
