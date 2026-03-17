package com.us.server.service;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.us.server.dto.LoginRequest;
import com.us.server.dto.LoginResponse;
import com.us.server.dto.RegisterRequest;
import com.us.server.dto.UserDto;
import com.us.server.dto.UserEventDto;
import com.us.server.entity.User;
import com.us.server.event.UserEventPublisher;
import com.us.server.repository.UserRepository;
import com.us.server.util.JwtUtil;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log =
        LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserEventPublisher eventPublisher;

    @Override
    @CacheEvict(value = "allUsers", allEntries = true)
    public UserDto register(RegisterRequest request) {
        log.info("Registering user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
            passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
        log.info("User saved with id: {}", user.getId());

        // publish RabbitMQ event
        UserEventDto event = new UserEventDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name(),
            "USER_REGISTERED"
        );
        eventPublisher.publishUserRegistered(event);

        return toDto(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() ->
                new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(
            user.getEmail(),
            user.getRole().name()
        );

        log.info("Login successful: {}", request.getEmail());
        return new LoginResponse(token);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        log.info("Fetching user: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("User not found: " + id));
        return toDto(user);
    }

    @Override
    @CacheEvict(value = {"users", "allUsers"}, key = "#id")
    public UserDto updateUser(Long id, UserDto dto) {
        log.info("Updating user: {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException("User not found: " + id));

        if (dto.getName() != null) user.setName(dto.getName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());

        userRepository.save(user);
        return toDto(user);
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}