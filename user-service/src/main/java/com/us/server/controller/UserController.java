package com.us.server.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.us.server.dto.LoginRequest;
import com.us.server.dto.LoginResponse;
import com.us.server.dto.RegisterRequest;
import com.us.server.dto.UserDto;
import com.us.server.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // POST /api/users/register — PUBLIC
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    // POST /api/users/login — PUBLIC
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    // GET /api/users/{userId} — PROTECTED
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
            userService.getUserById(userId));
    }

    // PUT /api/users/{userId} — PROTECTED
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @RequestBody UserDto dto) {
        return ResponseEntity.ok(
            userService.updateUser(userId, dto));
    }
}