package com.cinebook.catalog.service;

import com.cinebook.catalog.dto.AddTheaterRequest;
import com.cinebook.catalog.dto.TheaterDto;
import com.cinebook.catalog.dto.TheaterMapDto;
import com.cinebook.catalog.entity.Movie;
import com.cinebook.catalog.entity.Theater;
import com.cinebook.catalog.exception.ApiException;
import com.cinebook.catalog.repository.MovieRepository;
import com.cinebook.catalog.repository.TheaterRepository;
import com.cinebook.catalog.util.GeoUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final MovieRepository movieRepository;

    public TheaterService(TheaterRepository theaterRepository, MovieRepository movieRepository) {
        this.theaterRepository = theaterRepository;
        this.movieRepository = movieRepository;
    }

    // ── public ────────────────────────────────────────────────────────────

    public List<TheaterDto> listActive() {
        return theaterRepository.findByIsActiveTrue().stream().map(t -> {
            TheaterDto dto = toDto(t);
            dto.setMovieCount((long) movieRepository.findByTheaterTheaterIdOrderByCreatedAtDesc(t.getTheaterId())
                    .stream().filter(m -> Boolean.TRUE.equals(m.getIsActive())).count());
            return dto;
        }).toList();
    }

    public List<TheaterDto> nearby(Double lat, Double lng, double radiusKm) {
        if (lat == null || lng == null) {
            return listActive();
        }
        List<Theater> geoTheaters = theaterRepository.findByIsActiveTrueAndLatitudeIsNotNullAndLongitudeIsNotNull();
        if (geoTheaters.isEmpty()) {
            return listActive();
        }

        List<TheaterDto> withinRadius = geoTheaters.stream()
                .map(t -> withDistance(t, lat, lng))
                .filter(d -> d.getDistanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(TheaterDto::getDistanceKm))
                .toList();

        if (!withinRadius.isEmpty()) return withinRadius;

        // Nothing within radius — fall back to "everything, sorted by distance" like the original did
        return geoTheaters.stream()
                .map(t -> withDistance(t, lat, lng))
                .sorted(Comparator.comparingDouble(TheaterDto::getDistanceKm))
                .toList();
    }

    public List<TheaterMapDto> forMap() {
        return theaterRepository.findByIsActiveTrueAndLatitudeIsNotNullAndLongitudeIsNotNull().stream().map(t -> {
            TheaterMapDto dto = new TheaterMapDto();
            dto.setTheaterId(t.getTheaterId());
            dto.setTheaterName(t.getTheaterName());
            dto.setLocation(t.getLocation());
            dto.setCity(t.getCity());
            dto.setLatitude(t.getLatitude());
            dto.setLongitude(t.getLongitude());
            dto.setPhone(t.getPhone());
            dto.setTotalSeats(t.getTotalSeats());
            List<Movie> movies = movieRepository.findByTheaterTheaterIdOrderByCreatedAtDesc(t.getTheaterId());
            dto.setMovies(movies.stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                    .limit(5)
                    .map(m -> new TheaterMapDto.MovieLite(m.getTitle(), m.getLanguage()))
                    .toList());
            return dto;
        }).toList();
    }

    private TheaterDto withDistance(Theater t, double lat, double lng) {
        TheaterDto dto = toDto(t);
        double dist = GeoUtil.haversineKm(lat, lng, t.getLatitude().doubleValue(), t.getLongitude().doubleValue());
        dto.setDistanceKm(Math.round(dist * 10.0) / 10.0);
        return dto;
    }

    private TheaterDto toDto(Theater t) {
        TheaterDto dto = new TheaterDto();
        dto.setTheaterId(t.getTheaterId());
        dto.setTheaterName(t.getTheaterName());
        dto.setLocation(t.getLocation());
        dto.setTotalSeats(t.getTotalSeats());
        dto.setPhone(t.getPhone());
        dto.setCity(t.getCity());
        dto.setLatitude(t.getLatitude());
        dto.setLongitude(t.getLongitude());
        return dto;
    }

    // ── owner ─────────────────────────────────────────────────────────────

    public List<TheaterDto> forOwner(Long ownerId) {
        return theaterRepository.findByOwnerId(ownerId).stream().map(this::toDto).toList();
    }

    @Transactional
    public Theater addTheater(Long ownerId, AddTheaterRequest req) {
        Theater theater = new Theater();
        theater.setTheaterName(req.getTheaterName().trim());
        theater.setLocation(req.getLocation().trim());
        theater.setTotalSeats(req.getTotalSeats());
        theater.setPhone(req.getPhone());
        theater.setCity(req.getCity());
        theater.setLatitude(req.getLatitude());
        theater.setLongitude(req.getLongitude());
        theater.setOwnerId(ownerId);
        theater.setIsActive(true);
        return theaterRepository.save(theater);
    }

    /** Ownership check reused by ScreenLayoutService, MovieService callers, etc. */
    public Theater getOwnedTheaterOrThrow(Long ownerId, Long theaterId) {
        return theaterRepository.findByTheaterIdAndOwnerId(theaterId, ownerId)
                .orElseThrow(() -> new ApiException("Unauthorized theater.", HttpStatus.FORBIDDEN));
    }
}
