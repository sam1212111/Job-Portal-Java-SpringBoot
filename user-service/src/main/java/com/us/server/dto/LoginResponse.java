package com.us.server.dto;

public class LoginResponse {

    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() { return "Login successful Token: "+token; }
}	