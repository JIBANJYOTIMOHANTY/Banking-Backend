package com.learning.Project.dto;

public class AuthResponse {
    private String token;
    private long expiresInMs;
    private String username;

    public AuthResponse() {
    }

    public AuthResponse(String token, long expiresInMs, String username) {
        this.token = token;
        this.expiresInMs = expiresInMs;
        this.username = username;
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
}
