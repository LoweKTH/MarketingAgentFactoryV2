package com.exjobb.backend.dto;

public class LoginResponse {
    private String jwt;
    private String message;

    public LoginResponse(String jwt, String message) {
        this.jwt = jwt;
        this.message = message;
    }

    public String getJwt() {
        return jwt;
    }

    public String getMessage() {
        return message;
    }


}