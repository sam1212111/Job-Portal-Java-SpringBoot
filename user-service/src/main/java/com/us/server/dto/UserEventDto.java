package com.us.server.dto;

public class UserEventDto {

    private Long userId;
    private String name;
    private String email;
    private String role;
    private String eventType;

    public UserEventDto() {}

    public UserEventDto(Long userId, String name,
                        String email, String role,
                        String eventType) {
        this.userId    = userId;
        this.name      = name;
        this.email     = email;
        this.role      = role;
        this.eventType = eventType;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getEventType() { return eventType; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setEventType(String eventType) { this.eventType = eventType; }
}