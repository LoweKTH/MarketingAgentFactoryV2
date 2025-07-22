package com.exjobb.backend.dto;

public class LoginResponse {
    private String jwt;
    private String message;

    public LoginResponse(String jwt, String message) {
        this.jwt = jwt;
        this.message = message;
    }

    // Getters
    public String getJwt() {
        return jwt;
    }

    public String getMessage() {
        return message;
    }

    // No setters needed if immutable
}