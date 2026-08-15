package com.cinebook.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AddMovieRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 150)
    private String title;

    @Size(max = 20)
    private String duration;

    @Size(max = 100)
    private String genre;

    @Size(max = 50)
    private String language;

    private String description;

    @Size(max = 500)
    private String posterUrl;

    @NotNull(message = "Theater is required")
    private Long theaterId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public Long getTheaterId() { return theaterId; }
    public void setTheaterId(Long theaterId) { this.theaterId = theaterId; }
}
