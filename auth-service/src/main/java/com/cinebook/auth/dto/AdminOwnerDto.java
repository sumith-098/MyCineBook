package com.cinebook.auth.dto;

public class AdminOwnerDto {
    private Long ownerId;
    private String name;
    private String email;

    public AdminOwnerDto() {}
    public AdminOwnerDto(Long ownerId, String name, String email) {
        this.ownerId = ownerId; this.name = name; this.email = email;
    }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
