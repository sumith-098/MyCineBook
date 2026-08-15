package com.cinebook.catalog.service;

import com.cinebook.catalog.dto.*;
import com.cinebook.catalog.entity.Movie;
import com.cinebook.catalog.entity.Theater;
import com.cinebook.catalog.exception.ApiException;
import com.cinebook.catalog.repository.MovieRepository;
import com.cinebook.catalog.repository.TheaterRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final PriceRangeService priceRangeService;
    private final ShowtimeService showtimeService;
    private final RatingClient ratingClient;
    private final PosterUploadService posterUploadService;

    public MovieService(MovieRepository movieRepository, TheaterRepository theaterRepository,
                         PriceRangeService priceRangeService, ShowtimeService showtimeService,
                         RatingClient ratingClient, PosterUploadService posterUploadService) {
        this.movieRepository = movieRepository;
        this.theaterRepository = theaterRepository;
        this.priceRangeService = priceRangeService;
        this.showtimeService = showtimeService;
        this.ratingClient = ratingClient;
        this.posterUploadService = posterUploadService;
    }

    // ── public ────────────────────────────────────────────────────────────

    public List<MovieSummaryDto> homeFeatured() {
        return toSummaries(movieRepository.findTop8ByIsActiveTrueOrderByCreatedAtDesc());
    }

    public List<MovieSummaryDto> search(String q) {
        List<Movie> movies = (q == null || q.isBlank())
                ? movieRepository.findByIsActiveTrueOrderByCreatedAtDesc()
                : movieRepository.findByIsActiveTrueAndTitleContainingIgnoreCaseOrIsActiveTrueAndGenreContainingIgnoreCase(q, q);
        return toSummaries(movies);
    }

    public MovieDetailDto detail(Long movieId) {
        Movie movie = movieRepository.findByMovieIdAndIsActiveTrue(movieId)
                .orElseThrow(() -> new ApiException("Movie not found.", HttpStatus.NOT_FOUND));
        Theater theater = movie.getTheater();

        MovieDetailDto dto = new MovieDetailDto();
        dto.setMovieId(movie.getMovieId());
        dto.setTitle(movie.getTitle());
        dto.setDuration(movie.getDuration());
        dto.setGenre(movie.getGenre());
        dto.setLanguage(movie.getLanguage());
        dto.setDescription(movie.getDescription());
        dto.setPosterUrl(movie.getPosterUrl());
        dto.setTheaterId(theater.getTheaterId());
        dto.setTheaterName(theater.getTheaterName());
        dto.setLocation(theater.getLocation());
        dto.setTotalSeats(theater.getTotalSeats());

        var range = priceRangeService.rangeForTheater(theater.getTheaterId());
        dto.setPriceMin(range.min());
        dto.setPriceMax(range.max());
        dto.setShowtimes(showtimeService.upcomingForMovie(movieId));

        var rating = ratingClient.getRating(movieId);
        dto.setAvgRating(rating.avgRating());
        dto.setReviewCount(rating.reviewCount());
        return dto;
    }

    private List<MovieSummaryDto> toSummaries(List<Movie> movies) {
        return movies.stream().map(m -> {
            MovieSummaryDto dto = new MovieSummaryDto();
            dto.setMovieId(m.getMovieId());
            dto.setTitle(m.getTitle());
            dto.setDuration(m.getDuration());
            dto.setGenre(m.getGenre());
            dto.setLanguage(m.getLanguage());
            dto.setPosterUrl(m.getPosterUrl());
            dto.setIsActive(m.getIsActive());
            dto.setTheaterId(m.getTheater().getTheaterId());
            dto.setTheaterName(m.getTheater().getTheaterName());
            dto.setLocation(m.getTheater().getLocation());
            var range = priceRangeService.rangeForTheater(m.getTheater().getTheaterId());
            dto.setPriceMin(range.min());
            dto.setPriceMax(range.max());
            var rating = ratingClient.getRating(m.getMovieId());
            dto.setAvgRating(rating.avgRating());
            dto.setReviewCount(rating.reviewCount());
            return dto;
        }).toList();
    }

    // ── owner ─────────────────────────────────────────────────────────────

    public List<MovieSummaryDto> forOwner(Long ownerId) {
        List<Long> theaterIds = theaterRepository.findByOwnerId(ownerId).stream().map(Theater::getTheaterId).toList();
        if (theaterIds.isEmpty()) return List.of();
        return toSummaries(movieRepository.findByTheaterTheaterIdInOrderByCreatedAtDesc(theaterIds));
    }

    @Transactional
    public Movie addMovie(Long ownerId, AddMovieRequest req) {
        Theater theater = theaterRepository.findByTheaterIdAndOwnerId(req.getTheaterId(), ownerId)
                .orElseThrow(() -> new ApiException("Unauthorized theater.", HttpStatus.FORBIDDEN));

        Movie movie = new Movie();
        movie.setTitle(req.getTitle().trim());
        movie.setDuration(req.getDuration());
        movie.setGenre(req.getGenre());
        movie.setLanguage(req.getLanguage() == null || req.getLanguage().isBlank() ? "Tamil" : req.getLanguage());
        movie.setDescription(req.getDescription());
        movie.setPosterUrl(req.getPosterUrl());
        movie.setTheater(theater);
        movie.setIsActive(true);
        return movieRepository.save(movie);
    }

    @Transactional
    public void toggleActive(Long ownerId, Long movieId) {
        Movie movie = getOwnedMovieOrThrow(ownerId, movieId);
        movie.setIsActive(!Boolean.TRUE.equals(movie.getIsActive()));
        movieRepository.save(movie);
    }

    /** Owner replaces/sets the movie's poster photo — this is the "movie theme" feature: a real
     *  uploaded image, validated and stored by PosterUploadService, not just a pasted URL. */
    @Transactional
    public String uploadPoster(Long ownerId, Long movieId, MultipartFile file) {
        Movie movie = getOwnedMovieOrThrow(ownerId, movieId);
        String url = posterUploadService.store(file);
        movie.setPosterUrl(url);
        movieRepository.save(movie);
        return url;
    }

    /** Used by ShowtimeService/ScreenLayoutService to verify a movie belongs to this owner. */
    public Movie getOwnedMovieOrThrow(Long ownerId, Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ApiException("Movie not found.", HttpStatus.NOT_FOUND));
        if (!movie.getTheater().getOwnerId().equals(ownerId)) {
            throw new ApiException("Unauthorized.", HttpStatus.FORBIDDEN);
        }
        return movie;
    }
}
