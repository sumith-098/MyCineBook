package com.cinebook.catalog.controller;

import com.cinebook.catalog.dto.*;
import com.cinebook.catalog.service.MovieService;
import com.cinebook.catalog.service.ShowtimeService;
import com.cinebook.catalog.service.TheaterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class PublicCatalogController {

    private final MovieService movieService;
    private final TheaterService theaterService;
    private final ShowtimeService showtimeService;

    public PublicCatalogController(MovieService movieService, TheaterService theaterService, ShowtimeService showtimeService) {
        this.movieService = movieService;
        this.theaterService = theaterService;
        this.showtimeService = showtimeService;
    }

    @GetMapping("/movies/featured")
    public ApiResponse<List<MovieSummaryDto>> featured() {
        return ApiResponse.ok("OK", movieService.homeFeatured());
    }

    @GetMapping("/movies")
    public ApiResponse<List<MovieSummaryDto>> search(@RequestParam(required = false, name = "q") String query) {
        return ApiResponse.ok("OK", movieService.search(query));
    }

    @GetMapping("/movies/{movieId}")
    public ApiResponse<MovieDetailDto> detail(@PathVariable Long movieId) {
        return ApiResponse.ok("OK", movieService.detail(movieId));
    }

    @GetMapping("/theaters")
    public ApiResponse<List<TheaterDto>> theaters() {
        return ApiResponse.ok("OK", theaterService.listActive());
    }

    @GetMapping("/theaters/nearby")
    public ApiResponse<List<TheaterDto>> nearby(@RequestParam(required = false) Double lat,
                                                  @RequestParam(required = false) Double lng,
                                                  @RequestParam(required = false, defaultValue = "50") double radius) {
        return ApiResponse.ok("OK", theaterService.nearby(lat, lng, radius));
    }

    @GetMapping("/theaters/map")
    public ApiResponse<List<TheaterMapDto>> map() {
        return ApiResponse.ok("OK", theaterService.forMap());
    }

    @GetMapping("/showtimes/{showtimeId}")
    public ApiResponse<ShowtimeInfoDto> showtimeInfo(@PathVariable Long showtimeId) {
        return ApiResponse.ok("OK", showtimeService.getInfo(showtimeId));
    }
}
