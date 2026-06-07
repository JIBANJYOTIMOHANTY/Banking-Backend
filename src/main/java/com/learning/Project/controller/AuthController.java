package com.learning.Project.controller;

import com.learning.Project.dto.ApiResponse;
import com.learning.Project.dto.AuthResponse;
import com.learning.Project.dto.LoginRequest;
import com.learning.Project.dto.RegisterRequest;
import com.learning.Project.model.User;
import com.learning.Project.service.UserService;
import com.learning.Project.config.JwtTokenUtil;
import com.learning.Project.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT login")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user and automatically creates an associated bank account.")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody RegisterRequest request) {
        User registeredUser = userService.registerUser(request);
        // Clean password before returning response
        registeredUser.setPassword(null);
        return ResponseEntity.ok(new ApiResponse<>(0, "Registration successful", List.of(registeredUser)));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT", description = "Logs in a user, returning a JWT token valid for 10 minutes of activity.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String canonicalUsername = userDetails.getUsername();
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Retrieve the old token (if any) and delete it from Redis to invalidate it
        String activeTokenKey = "user_active_token:" + canonicalUsername;
        String oldToken = redisTemplate.opsForValue().get(activeTokenKey);
        if (oldToken != null) {
            redisTemplate.delete("jwt_session:" + oldToken);
        }

        // Store session in Redis with 10 minutes sliding TTL
        String sessionKey = "jwt_session:" + token;
        redisTemplate.opsForValue().set(sessionKey, canonicalUsername, Duration.ofMinutes(10));

        // Update the active token mapping with 10 minutes TTL
        redisTemplate.opsForValue().set(activeTokenKey, token, Duration.ofMinutes(10));

        AuthResponse authResponse = new AuthResponse(
                token,
                JwtTokenUtil.JWT_TOKEN_VALIDITY,
                canonicalUsername);

        return ResponseEntity.ok(new ApiResponse<>(0, "Login successful", List.of(authResponse)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and invalidate JWT", description = "Logs out the user and invalidates the JWT session in Redis.")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String jwtToken = requestTokenHeader.substring(7).trim();
            if (jwtToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
                jwtToken = jwtToken.substring(7).trim();
            }
            try {
                String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                if (username != null) {
                    // Delete the specific session key
                    String sessionKey = "jwt_session:" + jwtToken;
                    redisTemplate.delete(sessionKey);

                    // Only delete the active token mapping if it matches the token being invalidated
                    String activeTokenKey = "user_active_token:" + username;
                    String activeToken = redisTemplate.opsForValue().get(activeTokenKey);
                    if (jwtToken.equals(activeToken)) {
                        redisTemplate.delete(activeTokenKey);
                    }
                    return ResponseEntity.ok(new ApiResponse<>(0, "Logout successful", List.of("Session invalidated successfully")));
                }
            } catch (Exception e) {
                // Token might be malformed or already expired, we still treat logout as successful
            }
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(1, "Invalid or missing token", List.of()));
    }
}
