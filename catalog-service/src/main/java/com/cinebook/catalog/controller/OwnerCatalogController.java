package com.cinebook.catalog.controller;

import com.cinebook.catalog.dto.*;
import com.cinebook.catalog.entity.Movie;
import com.cinebook.catalog.entity.Theater;
import com.cinebook.catalog.service.MovieService;
import com.cinebook.catalog.service.ScreenLayoutService;
import com.cinebook.catalog.service.ShowtimeService;
import com.cinebook.catalog.service.TheaterService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Every endpoint here requires ROLE_OWNER (enforced in SecurityConfig) and additionally
 * verifies the authenticated owner actually owns the theater/movie being acted on
 * (enforced in the service layer) — the JWT alone is not enough authorization.
 */
@RestController
@RequestMapping("/api/catalog/owner")
public class OwnerCatalogController {

    private final TheaterService theaterService;
    private final MovieService movieService;
    private final ShowtimeService showtimeService;
    private final ScreenLayoutService screenLayoutService;

    public OwnerCatalogController(TheaterService theaterService, MovieService movieService,
                                   ShowtimeService showtimeService, ScreenLayoutService screenLayoutService) {
        this.theaterService = theaterService;
        this.movieService = movieService;
        this.showtimeService = showtimeService;
        this.screenLayoutService = screenLayoutService;
    }

    private Long ownerId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }

    @GetMapping("/dashboard")
    public ApiResponse<OwnerDashboardDto> dashboard(Authentication auth) {
        OwnerDashboardDto dto = new OwnerDashboardDto();
        dto.setTheaters(theaterService.forOwner(ownerId(auth)));
        dto.setMovies(movieService.forOwner(ownerId(auth)));
        return ApiResponse.ok("OK", dto);
    }

    @GetMapping("/theaters")
    public ApiResponse<List<TheaterDto>> myTheaters(Authentication auth) {
        return ApiResponse.ok("OK", theaterService.forOwner(ownerId(auth)));
    }

    @PostMapping("/theaters")
    public ApiResponse<Map<String, Long>> addTheater(Authentication auth, @Valid @RequestBody AddTheaterRequest req) {
        Theater theater = theaterService.addTheater(ownerId(auth), req);
        return ApiResponse.ok("Theater added! It will appear on the map.", Map.of("theaterId", theater.getTheaterId()));
    }

    @GetMapping("/movies")
    public ApiResponse<List<MovieSummaryDto>> myMovies(Authentication auth) {
        return ApiResponse.ok("OK", movieService.forOwner(ownerId(auth)));
    }

    @PostMapping("/movies")
    public ApiResponse<Map<String, Long>> addMovie(Authentication auth, @Valid @RequestBody AddMovieRequest req) {
        Movie movie = movieService.addMovie(ownerId(auth), req);
        return ApiResponse.ok("Movie added! Now add showtimes and set ticket prices in Screen Layout.",
                Map.of("movieId", movie.getMovieId()));
    }

    @PatchMapping("/movies/{movieId}/toggle")
    public ApiResponse<Void> toggleMovie(Authentication auth, @PathVariable Long movieId) {
        movieService.toggleActive(ownerId(auth), movieId);
        return ApiResponse.ok("Movie status updated.");
    }

    /** Real image upload for the movie poster — validated by file content (not filename/header),
     *  capped at 5MB, only JPEG/PNG/WEBP accepted. See PosterUploadService for details. */
    @PostMapping(value = "/movies/{movieId}/poster", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> uploadPoster(Authentication auth, @PathVariable Long movieId,
                                                           @org.springframework.web.bind.annotation.RequestParam("file")
                                                           org.springframework.web.multipart.MultipartFile file) {
        String url = movieService.uploadPoster(ownerId(auth), movieId, file);
        return ApiResponse.ok("Poster uploaded!", Map.of("posterUrl", url));
    }

    @GetMapping("/movies/{movieId}/showtimes")
    public ApiResponse<List<ShowtimeDto>> showtimes(Authentication auth, @PathVariable Long movieId) {
        return ApiResponse.ok("OK", showtimeService.allForOwner(ownerId(auth), movieId));
    }

    @PostMapping("/movies/{movieId}/showtimes")
    public ApiResponse<ShowtimeDto> addShowtime(Authentication auth, @PathVariable Long movieId,
                                                 @Valid @RequestBody AddShowtimeRequest req) {
        ShowtimeDto dto = showtimeService.add(ownerId(auth), movieId, req);
        return ApiResponse.ok("Showtime added!", dto);
    }

    @GetMapping("/theaters/{theaterId}/screen-layout")
    public ApiResponse<List<ScreenLayoutResponseDto>> screenLayouts(Authentication auth, @PathVariable Long theaterId) {
        return ApiResponse.ok("OK", screenLayoutService.listForOwner(ownerId(auth), theaterId));
    }

    @PutMapping("/theaters/{theaterId}/screen-layout")
    public ApiResponse<ScreenLayoutService.SaveResult> saveScreenLayout(Authentication auth, @PathVariable Long theaterId,
                                                                          @Valid @RequestBody ScreenLayoutRequest req) {
        ScreenLayoutService.SaveResult result = screenLayoutService.save(ownerId(auth), theaterId, req);
        String message = result.saved()
                ? "Screen layout for " + req.getScreenName() + " saved!"
                : "⚠️ " + result.activeBookingCount() + " active booking(s) exist for " + req.getScreenName()
                  + ". Resubmit with forceSave=true to confirm.";
        return ApiResponse.ok(message, result);
    }
}
