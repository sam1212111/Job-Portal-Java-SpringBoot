package com.us.server.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.us.server.entity.User;
import com.us.server.repository.UserRepository;
import com.us.server.service.UserService;

@Component("authzService")
public class AuthzService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    public boolean canAccessUser(String externalUserId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) {
            return true;
        }

        String authenticatedEmail = authentication.getName();

        Long internalId = userService.parseExternalUserId(externalUserId);
        User targetUser = userRepository.findById(internalId).orElse(null);

        return targetUser != null && targetUser.getEmail().equalsIgnoreCase(authenticatedEmail);
    }
}