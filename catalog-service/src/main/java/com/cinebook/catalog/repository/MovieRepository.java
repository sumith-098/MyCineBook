package com.cinebook.catalog.repository;

import com.cinebook.catalog.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByIsActiveTrueOrderByCreatedAtDesc();
    List<Movie> findTop8ByIsActiveTrueOrderByCreatedAtDesc();
    List<Movie> findByIsActiveTrueAndTitleContainingIgnoreCaseOrIsActiveTrueAndGenreContainingIgnoreCase(String title, String genre);
    Optional<Movie> findByMovieIdAndIsActiveTrue(Long movieId);
    List<Movie> findByTheaterTheaterIdOrderByCreatedAtDesc(Long theaterId);
    List<Movie> findByTheaterTheaterIdInOrderByCreatedAtDesc(List<Long> theaterIds);
}
