package com.cinebook.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogStatsLiteDto {
    private long theaterCount;
    private long movieCount;

    public long getTheaterCount() { return theaterCount; }
    public void setTheaterCount(long theaterCount) { this.theaterCount = theaterCount; }
    public long getMovieCount() { return movieCount; }
    public void setMovieCount(long movieCount) { this.movieCount = movieCount; }
}
