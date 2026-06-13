package com.learning.Project.controller;

import com.learning.Project.dto.ApiResponse;
import com.learning.Project.dto.AuthResponse;
import com.learning.Project.dto.LoginRequest;
import com.learning.Project.dto.RegisterRequest;
import com.learning.Project.model.User;
import com.learning.Project.service.UserService;
import com.learning.Project.config.JwtTokenUtil;
import com.learning.Project.service.CustomUserDetailsService;
import com.learning.Project.service.SessionLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final StringRedisTemplate redisTemplate;
    private final SessionLogService sessionLogService;

    AuthController(AuthenticationManager authenticationManager, UserService userService,
            CustomUserDetailsService userDetailsService, JwtTokenUtil jwtTokenUtil, StringRedisTemplate redisTemplate,
            SessionLogService sessionLogService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.redisTemplate = redisTemplate;
        this.sessionLogService = sessionLogService;
    }

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
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
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

        // Store session in Redis with sliding TTL from configuration
        String sessionKey = "jwt_session:" + token;
        redisTemplate.opsForValue().set(sessionKey, canonicalUsername, jwtTokenUtil.getValidityDuration());

        // Update the active token mapping with TTL from configuration
        redisTemplate.opsForValue().set(activeTokenKey, token, jwtTokenUtil.getValidityDuration());

        String firstName = null;
        String lastName = null;
        String role = null;
        String profileImage = null;
        try {
            User user = userService.findByUsername(canonicalUsername);
            if (user != null) {
                firstName = user.getFirstName();
                lastName = user.getLastName();
                role = user.getRole();
                profileImage = user.getProfileImage();
            }
        } catch (Exception e) {
            // Ignore and do not share if failed
        }

        AuthResponse authResponse = new AuthResponse(
                token,
                jwtTokenUtil.getValidityDuration().toMillis(),
                canonicalUsername,
                firstName,
                lastName,
                role,
                profileImage);

        try {
            String userAgent = servletRequest.getHeader("User-Agent");
            String ipAddress = servletRequest.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
            sessionLogService.logActivity(
                    canonicalUsername,
                    getBrowserAndOs(userAgent),
                    getDeviceIcon(userAgent),
                    ipAddress,
                    "Admin logged in successfully",
                    "Active");
        } catch (Exception e) {
            // Ignore logging failures to avoid blocking login flow
        }

        return ResponseEntity.ok(new ApiResponse<>(0, "Login successful", List.of(authResponse)));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Generates a new JWT token using the existing valid token.")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(HttpServletRequest request) {
        String requestTokenHeader = request.getHeader("Authorization");
        if (requestTokenHeader != null && requestTokenHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String jwtToken = requestTokenHeader.substring(7).trim();
            if (jwtToken.regionMatches(true, 0, "Bearer ", 0, 7)) {
                jwtToken = jwtToken.substring(7).trim();
            }
            try {
                String sessionKey = "jwt_session:" + jwtToken;
                Boolean hasSession = redisTemplate.hasKey(sessionKey);
                if (Boolean.TRUE.equals(hasSession)) {
                    String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    String newToken = jwtTokenUtil.generateToken(userDetails);

                    redisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(10));
                    String activeTokenKey = "user_active_token:" + username;
                    redisTemplate.delete(activeTokenKey);

                    String newSessionKey = "jwt_session:" + newToken;
                    redisTemplate.opsForValue().set(newSessionKey, username, jwtTokenUtil.getValidityDuration());
                    redisTemplate.opsForValue().set(activeTokenKey, newToken, jwtTokenUtil.getValidityDuration());

                    String firstName = null;
                    String lastName = null;
                    String role = null;
                    String profileImage = null;
                    try {
                        User user = userService.findByUsername(username);
                        if (user != null) {
                            firstName = user.getFirstName();
                            lastName = user.getLastName();
                            role = user.getRole();
                            profileImage = user.getProfileImage();
                        }
                    } catch (Exception e) {
                        // Ignore and do not share if failed
                    }

                    AuthResponse authResponse = new AuthResponse(
                            newToken,
                            jwtTokenUtil.getValidityDuration().toMillis(),
                            username,
                            firstName,
                            lastName,
                            role,
                            profileImage);

                    try {
                        String userAgent = request.getHeader("User-Agent");
                        String ipAddress = request.getRemoteAddr();
                        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                            ipAddress = "127.0.0.1";
                        }
                        sessionLogService.logActivity(
                                username,
                                getBrowserAndOs(userAgent),
                                getDeviceIcon(userAgent),
                                ipAddress,
                                "Admin session token refreshed",
                                "Active");
                    } catch (Exception e) {
                    }

                    return ResponseEntity
                            .ok(new ApiResponse<>(0, "Token refreshed successfully", List.of(authResponse)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.status(401).body(new ApiResponse<>(1, "Invalid or expired token", List.of()));
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

                    // Only delete the active token mapping if it matches the token being
                    // invalidated
                    String activeTokenKey = "user_active_token:" + username;
                    String activeToken = redisTemplate.opsForValue().get(activeTokenKey);
                    if (jwtToken.equals(activeToken)) {
                        redisTemplate.delete(activeTokenKey);
                    }
                    try {
                        String userAgent = request.getHeader("User-Agent");
                        String ipAddress = request.getRemoteAddr();
                        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                            ipAddress = "127.0.0.1";
                        }
                        sessionLogService.logActivity(
                                username,
                                getBrowserAndOs(userAgent),
                                getDeviceIcon(userAgent),
                                ipAddress,
                                "Admin logged out successfully",
                                "Terminated");
                    } catch (Exception e) {
                    }

                    return ResponseEntity
                            .ok(new ApiResponse<>(0, "Logout successful", List.of("Session invalidated successfully")));
                }
            } catch (Exception e) {
                // Token might be malformed or already expired, we still treat logout as
                // successful
            }
        }
        return ResponseEntity.badRequest().body(new ApiResponse<>(1, "Invalid or missing token", List.of()));
    }

    private String getBrowserAndOs(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Chrome (Windows 11)";
        }
        String browser = "Chrome";
        String os = "Windows 11";

        if (userAgent.contains("Win")) {
            if (userAgent.contains("Windows NT 10.0"))
                os = "Windows 11";
            else
                os = "Windows";
        } else if (userAgent.contains("Mac")) {
            if (userAgent.contains("iPhone") || userAgent.contains("iPad"))
                os = "iOS";
            else
                os = "macOS";
        } else if (userAgent.contains("X11") || userAgent.contains("Linux")) {
            os = "Linux";
        } else if (userAgent.contains("Android")) {
            os = "Android";
        }

        if (userAgent.contains("Chrome") && !userAgent.contains("Chromium") && !userAgent.contains("Edg")) {
            browser = "Chrome";
        } else if (userAgent.contains("Safari") && !userAgent.contains("Chrome") && !userAgent.contains("Chromium")) {
            browser = "Safari";
        } else if (userAgent.contains("Firefox")) {
            browser = "Firefox";
        } else if (userAgent.contains("Edg")) {
            browser = "Microsoft Edge";
        }
        return browser + " (" + os + ")";
    }

    private String getDeviceIcon(String userAgent) {
        if (userAgent == null)
            return "laptop_windows";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad") || userAgent.contains("Android")) {
            return "smartphone";
        }
        return "laptop_windows";
    }
}
