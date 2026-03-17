package com.us.server.service;

import com.us.server.dto.*;

public interface UserService {

    UserDto register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserDto getUserById(Long id);

    UserDto updateUser(Long id, UserDto dto);
}