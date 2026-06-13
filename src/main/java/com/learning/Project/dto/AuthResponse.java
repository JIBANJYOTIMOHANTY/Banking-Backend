package com.learning.Project.dto;

public class AuthResponse {
    private String token;
    private long expiresInMs;
    private String username;
    private String firstName;
    private String lastName;
    private String role;
    private String profileImage;

    public AuthResponse() {
    }

    public AuthResponse(String token, long expiresInMs, String username) {
        this.token = token;
        this.expiresInMs = expiresInMs;
        this.username = username;
    }

    public AuthResponse(String token, long expiresInMs, String username, String firstName, String lastName) {
        this.token = token;
        this.expiresInMs = expiresInMs;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public AuthResponse(String token, long expiresInMs, String username, String firstName, String lastName, String role) {
        this.token = token;
        this.expiresInMs = expiresInMs;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public AuthResponse(String token, long expiresInMs, String username, String firstName, String lastName, String role, String profileImage) {
        this.token = token;
        this.expiresInMs = expiresInMs;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.profileImage = profileImage;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}

