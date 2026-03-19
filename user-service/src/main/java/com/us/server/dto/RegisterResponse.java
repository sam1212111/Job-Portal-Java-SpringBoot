package com.us.server.dto;

public class RegisterResponse {

    private String userId;
    private String message;

    public RegisterResponse() {}

    public RegisterResponse(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;	
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}