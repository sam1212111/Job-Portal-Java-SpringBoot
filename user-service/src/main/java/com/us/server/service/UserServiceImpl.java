package com.us.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.us.server.dto.LoginRequest;
import com.us.server.dto.LoginResponse;
import com.us.server.dto.RegisterRequest;
import com.us.server.dto.RegisterResponse;
import com.us.server.dto.UpdateUserRequest;
import com.us.server.dto.UserDto;
import com.us.server.dto.UserEventDto;
import com.us.server.entity.User;
import com.us.server.event.UserEventPublisher;
import com.us.server.exception.EmailAlreadyExistsException;
import com.us.server.exception.InvalidCredentialsException;
import com.us.server.exception.UserNotFoundException;
import com.us.server.repository.UserRepository;
import com.us.server.util.JwtUtil;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String USER_ID_PREFIX = "U";

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
    public RegisterResponse register(RegisterRequest request) {
        log.info("Registering user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        user = userRepository.save(user);
        log.info("User saved with id: {}", user.getId());

        UserEventDto event = new UserEventDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                "USER_REGISTERED"
        );
        eventPublisher.publishUserRegistered(event);

        return new RegisterResponse(toExternalUserId(user.getId()), "User registered successfully");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token);
    }

    @Override
    @Cacheable(value = "users", key = "#externalUserId")
    public UserDto getUserById(String externalUserId) {
        Long id = parseExternalUserId(externalUserId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + externalUserId));

        return toDto(user);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#externalUserId"),
            @CacheEvict(value = "allUsers", allEntries = true)
    })
    public UserDto updateUser(String externalUserId, UpdateUserRequest request) {
        Long id = parseExternalUserId(externalUserId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + externalUserId));

        if (request.getEmail() != null &&
                !request.getEmail().isBlank() &&
                userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        user = userRepository.save(user);
        return toDto(user);
    }

    @Override
    public Long parseExternalUserId(String externalUserId) {
        if (externalUserId == null || externalUserId.isBlank()) {
            throw new UserNotFoundException("Invalid userId");
        }

        String normalized = externalUserId.trim().toUpperCase();
        if (!normalized.startsWith(USER_ID_PREFIX)) {
            throw new UserNotFoundException("Invalid userId format. Expected format like U101");
        }

        try {
            return Long.parseLong(normalized.substring(1));
        } catch (NumberFormatException ex) {
            throw new UserNotFoundException("Invalid userId format. Expected format like U101");
        }
    }

    private String toExternalUserId(Long id) {
        return USER_ID_PREFIX + id;
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId()); // internal numeric id; keep for compatibility
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }
}