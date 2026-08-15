package com.cinebook.catalog.dto;

import java.util.List;

/** Theaters + movies only — booking/revenue stats are booking-service's data and are fetched
 *  and merged client-side (or by a future BFF/gateway aggregator), not duplicated here. */
public class OwnerDashboardDto {
    private List<TheaterDto> theaters;
    private List<MovieSummaryDto> movies;

    public List<TheaterDto> getTheaters() { return theaters; }
    public void setTheaters(List<TheaterDto> theaters) { this.theaters = theaters; }
    public List<MovieSummaryDto> getMovies() { return movies; }
    public void setMovies(List<MovieSummaryDto> movies) { this.movies = movies; }
}
