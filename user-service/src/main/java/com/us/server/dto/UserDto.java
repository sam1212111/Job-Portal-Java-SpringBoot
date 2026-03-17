package com.us.server.dto;

import com.us.server.entity.User.Role;

public class UserDto {

    private Long id;
    private String name;
    private String email;
    private Role role;

    public UserDto() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(Role role) { this.role = role; }
}