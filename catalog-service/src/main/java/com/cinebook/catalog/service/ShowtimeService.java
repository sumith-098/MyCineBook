package com.cinebook.catalog.service;

import com.cinebook.catalog.dto.AddShowtimeRequest;
import com.cinebook.catalog.dto.ShowtimeDto;
import com.cinebook.catalog.dto.ShowtimeInfoDto;
import com.cinebook.catalog.entity.Movie;
import com.cinebook.catalog.entity.ScreenLayout;
import com.cinebook.catalog.entity.SeatCategory;
import com.cinebook.catalog.entity.Showtime;
import com.cinebook.catalog.exception.ApiException;
import com.cinebook.catalog.repository.MovieRepository;
import com.cinebook.catalog.repository.ScreenLayoutRepository;
import com.cinebook.catalog.repository.SeatCategoryRepository;
import com.cinebook.catalog.repository.ShowtimeRepository;
import com.cinebook.catalog.util.DurationParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShowtimeService {

    private final MovieRepository movieRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ScreenLayoutRepository screenLayoutRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final ObjectMapper objectMapper;

    public ShowtimeService(MovieRepository movieRepository, ShowtimeRepository showtimeRepository,
                            ScreenLayoutRepository screenLayoutRepository, SeatCategoryRepository seatCategoryRepository,
                            ObjectMapper objectMapper) {
        this.movieRepository = movieRepository;
        this.showtimeRepository = showtimeRepository;
        this.screenLayoutRepository = screenLayoutRepository;
        this.seatCategoryRepository = seatCategoryRepository;
        this.objectMapper = objectMapper;
    }

    public ShowtimeInfoDto getInfo(Long showtimeId) {
        Showtime s = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ApiException("Showtime not found.", HttpStatus.NOT_FOUND));
        Movie movie = s.getMovie();
        var theater = movie.getTheater();

        ShowtimeInfoDto dto = new ShowtimeInfoDto();
        dto.setShowtimeId(s.getShowtimeId());
        dto.setMovieId(movie.getMovieId());
        dto.setMovieTitle(movie.getTitle());
        dto.setShowDate(s.getShowDate().toString());
        dto.setShowTime(s.getShowTime().toString());
        dto.setScreen(s.getScreen());
        dto.setTheaterId(theater.getTheaterId());
        dto.setTheaterName(theater.getTheaterName());
        dto.setLocation(theater.getLocation());
        dto.setTotalSeats(theater.getTotalSeats());

        screenLayoutRepository.findByTheaterIdAndScreenName(theater.getTheaterId(), s.getScreen())
                .ifPresentOrElse(layout -> {
                    try {
                        dto.setLayout(layout.getLayoutJson() == null ? null
                                : objectMapper.readValue(layout.getLayoutJson(), Object.class));
                    } catch (Exception e) {
                        dto.setLayout(null);
                    }
                }, () -> dto.setLayout(null));

        List<SeatCategory> cats = seatCategoryRepository
                .findByTheaterIdAndScreenNameOrderBySortOrder(theater.getTheaterId(), s.getScreen());
        Map<String, ShowtimeInfoDto.CatPrice> catPrices = new HashMap<>();
        for (SeatCategory c : cats) {
            catPrices.put(c.getCategory(), new ShowtimeInfoDto.CatPrice(c.getPrice(), c.getColorCode()));
        }
        dto.setCatPrices(catPrices);
        return dto;
    }

    public List<ShowtimeDto> upcomingForMovie(Long movieId) {
        LocalDateTime now = LocalDateTime.now();
        return showtimeRepository
                .findByMovieMovieIdAndShowDateGreaterThanEqualOrderByShowDateAscShowTimeAsc(movieId, now.toLocalDate())
                .stream()
                .filter(s -> LocalDateTime.of(s.getShowDate(), s.getShowTime()).isAfter(now))
                .map(this::toDto)
                .toList();
    }

    public List<ShowtimeDto> allForOwner(Long ownerId, Long movieId) {
        Movie movie = ownedMovieOrThrow(ownerId, movieId);
        return showtimeRepository.findByMovieMovieIdOrderByShowDateAscShowTimeAsc(movie.getMovieId())
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public ShowtimeDto add(Long ownerId, Long movieId, AddShowtimeRequest req) {
        Movie movie = ownedMovieOrThrow(ownerId, movieId);

        LocalDate showDate;
        LocalTime showTime;
        try {
            showDate = LocalDate.parse(req.getShowDate());
            showTime = LocalTime.parse(req.getShowTime());
        } catch (Exception e) {
            throw new ApiException("Invalid date/time format.", HttpStatus.BAD_REQUEST);
        }
        if (LocalDateTime.of(showDate, showTime).isBefore(LocalDateTime.now())) {
            throw new ApiException("Showtime must be in the future.", HttpStatus.BAD_REQUEST);
        }

        String screen = req.getScreen() == null || req.getScreen().isBlank() ? "Screen 1" : req.getScreen().trim();
        Long theaterId = movie.getTheater().getTheaterId();

        // Runtime-aware overlap check: a screen can only show one film at a time, so the new
        // showtime's [start, start+duration) window must not intersect any existing showtime's
        // window on that same screen and day. Duration comes from the free-text field the owner
        // typed (e.g. "2h 30m") via DurationParser — see its javadoc for the fallback behavior.
        LocalDateTime newStart = LocalDateTime.of(showDate, showTime);
        LocalDateTime newEnd = newStart.plusMinutes(DurationParser.toMinutes(movie.getDuration()));

        List<Showtime> sameScreenSameDay = showtimeRepository
                .findByMovie_Theater_TheaterIdAndScreenAndShowDate(theaterId, screen, showDate);
        for (Showtime existing : sameScreenSameDay) {
            LocalDateTime existingStart = LocalDateTime.of(existing.getShowDate(), existing.getShowTime());
            LocalDateTime existingEnd = existingStart.plusMinutes(DurationParser.toMinutes(existing.getMovie().getDuration()));
            boolean overlaps = newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
            if (overlaps) {
                throw new ApiException(
                        screen + " is already showing \"" + existing.getMovie().getTitle() + "\" from "
                                + existing.getShowTime() + " that day — pick a time after " + existingEnd.toLocalTime() + ".",
                        HttpStatus.CONFLICT);
            }
        }

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setShowDate(showDate);
        showtime.setShowTime(showTime);
        showtime.setScreen(screen);
        return toDto(showtimeRepository.save(showtime));
    }

    private Movie ownedMovieOrThrow(Long ownerId, Long movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ApiException("Movie not found.", HttpStatus.NOT_FOUND));
        if (!movie.getTheater().getOwnerId().equals(ownerId)) {
            throw new ApiException("Unauthorized.", HttpStatus.FORBIDDEN);
        }
        return movie;
    }

    private ShowtimeDto toDto(Showtime s) {
        ShowtimeDto dto = new ShowtimeDto();
        dto.setShowtimeId(s.getShowtimeId());
        dto.setMovieId(s.getMovie().getMovieId());
        dto.setShowDate(s.getShowDate().toString());
        dto.setShowTime(s.getShowTime().toString());
        dto.setScreen(s.getScreen());
        return dto;
    }
}