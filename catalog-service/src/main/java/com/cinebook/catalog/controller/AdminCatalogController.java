package com.cinebook.catalog.controller;

import com.cinebook.catalog.dto.AdminCatalogStatsDto;
import com.cinebook.catalog.dto.AdminTheaterDto;
import com.cinebook.catalog.dto.ApiResponse;
import com.cinebook.catalog.repository.MovieRepository;
import com.cinebook.catalog.repository.TheaterRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** ROLE_ADMIN only (see SecurityConfig). Called by admin-service, which forwards the admin's
 *  own JWT rather than using a separate service key — these reads happen under the admin's
 *  own identity, same as any other admin action, and stay consistently auditable that way. */
@RestController
@RequestMapping("/api/catalog/admin")
public class AdminCatalogController {

    private final TheaterRepository theaterRepository;
    private final MovieRepository movieRepository;

    public AdminCatalogController(TheaterRepository theaterRepository, MovieRepository movieRepository) {
        this.theaterRepository = theaterRepository;
        this.movieRepository = movieRepository;
    }

    @GetMapping("/theaters")
    public ApiResponse<List<AdminTheaterDto>> allTheaters() {
        List<AdminTheaterDto> theaters = theaterRepository.findAll().stream()
                .map(t -> new AdminTheaterDto(t.getTheaterId(), t.getTheaterName(), t.getOwnerId()))
                .toList();
        return ApiResponse.ok("OK", theaters);
    }

    @GetMapping("/stats")
    public ApiResponse<AdminCatalogStatsDto> stats() {
        return ApiResponse.ok("OK", new AdminCatalogStatsDto(theaterRepository.count(), movieRepository.count()));
    }
}
