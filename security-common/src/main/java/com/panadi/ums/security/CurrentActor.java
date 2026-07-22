package com.panadi.ums.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record CurrentActor(UUID userId, Set<String> roles) {
    public static CurrentActor required() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt) || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("An authenticated Keycloak user is required");
        }
        UUID userId;
        try {
            userId = UUID.fromString(jwt.getToken().getSubject());
        } catch (RuntimeException exception) {
            throw new AccessDeniedException("The access token subject is not a valid Keycloak user id");
        }
        Set<String> roles = jwt.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .collect(Collectors.toUnmodifiableSet());
        return new CurrentActor(userId, roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
