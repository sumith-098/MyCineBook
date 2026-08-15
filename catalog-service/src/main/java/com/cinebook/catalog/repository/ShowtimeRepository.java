package com.cinebook.catalog.repository;

import com.cinebook.catalog.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    // "upcoming only" — mirrors the old TIMESTAMP(show_date, show_time) > NOW() filter
    List<Showtime> findByMovieMovieIdAndShowDateGreaterThanEqualOrderByShowDateAscShowTimeAsc(Long movieId, LocalDate today);

    List<Showtime> findByMovieMovieIdOrderByShowDateAscShowTimeAsc(Long movieId);

    List<Showtime> findByMovie_Theater_TheaterIdAndScreenAndShowDate(Long theaterId, String screen, LocalDate showDate);

}
