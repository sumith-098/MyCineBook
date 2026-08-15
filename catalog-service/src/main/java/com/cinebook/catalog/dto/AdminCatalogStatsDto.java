package com.cinebook.catalog.dto;

public class AdminCatalogStatsDto {
    private long theaterCount;
    private long movieCount;

    public AdminCatalogStatsDto() {}
    public AdminCatalogStatsDto(long theaterCount, long movieCount) {
        this.theaterCount = theaterCount; this.movieCount = movieCount;
    }
    public long getTheaterCount() { return theaterCount; }
    public void setTheaterCount(long theaterCount) { this.theaterCount = theaterCount; }
    public long getMovieCount() { return movieCount; }
    public void setMovieCount(long movieCount) { this.movieCount = movieCount; }
}
