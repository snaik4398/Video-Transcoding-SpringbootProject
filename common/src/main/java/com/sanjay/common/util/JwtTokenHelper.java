package com.sanjay.common.util;

import com.sanjay.common.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting user information from JWT tokens and SecurityContext.
 * This class provides common methods used across all microservices to extract
 * userId, username, role, and create User objects from JWT tokens.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenHelper {
    
    private final JwtUtil jwtUtil;
    
    /**
     * Extracts the userId from the JWT token in the Authorization header.
     * 
     * @param request HTTP servlet request containing the Authorization header
     * @return userId from token, or null if token is missing or invalid
     */
    public String getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }
    
    /**
     * Extracts the username from the SecurityContext (set by JwtAuthenticationFilter).
     * 
     * @return username from SecurityContext, or null if not authenticated
     */
    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
    
    /**
     * Extracts the user role from the JWT token in the Authorization header.
     * 
     * @param request HTTP servlet request containing the Authorization header
     * @return UserRole from token, or USER as default if role is missing or invalid
     */
    public User.UserRole getUserRoleFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String roleStr = jwtUtil.extractUserRole(token);
            if (roleStr != null) {
                try {
                    return User.UserRole.valueOf(roleStr);
                } catch (IllegalArgumentException e) {
                    return User.UserRole.USER;
                }
            }
        }
        return User.UserRole.USER;
    }
    
    /**
     * Creates a minimal User object from the JWT token and SecurityContext.
     * This is useful when service methods require a User entity but we only have
     * the JWT token information.
     * 
     * @param request HTTP servlet request containing the Authorization header
     * @return User object with id, username, role, and isActive=true, or null if userId cannot be extracted
     */
    public User createUserFromToken(HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        String username = getUsername();
        User.UserRole role = getUserRoleFromToken(request);
        
        if (userId == null) {
            return null;
        }
        
        return User.builder()
                .id(userId)
                .username(username)
                .role(role)
                .isActive(true)
                .build();
    }
    
    /**
     * Validates that a userId can be extracted from the token.
     * 
     * @param request HTTP servlet request containing the Authorization header
     * @return true if userId can be extracted, false otherwise
     */
    public boolean isAuthenticated(HttpServletRequest request) {
        return getUserIdFromToken(request) != null;
    }
}

