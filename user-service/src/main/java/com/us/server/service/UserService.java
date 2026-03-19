package com.us.server.service;

import com.us.server.dto.*;

public interface UserService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    UserDto getUserById(String externalUserId);

    UserDto updateUser(String externalUserId, UpdateUserRequest request);

    Long parseExternalUserId(String externalUserId);
}