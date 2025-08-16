package com.example.ats.controller;

import com.example.ats.model.AuthResponse;
import com.example.ats.model.LoginRequest;
import com.example.ats.model.RegisterRequest;
import com.example.ats.model.User;
import com.example.ats.security.JwtUtil;
import com.example.ats.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * Simple JSON registration endpoint (no files)
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try {
            userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(403).body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Handles login and returns JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userService.findByEmail(request.getEmail());
            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new AuthResponse(null, "Invalid email or password"));
        }
    }

    /**
     * Simple profile check endpoint
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return ResponseEntity.ok(Map.of("message", "Welcome to your dashboard!"));
    }
}
