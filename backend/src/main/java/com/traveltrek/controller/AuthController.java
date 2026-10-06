package com.traveltrek.controller;

import com.traveltrek.dto.AuthResponse;
import com.traveltrek.dto.LoginRequest;
import com.traveltrek.dto.RegisterRequest;
import com.traveltrek.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:63342",
        "https://traveltrek-dun.vercel.app"
})

public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    // POST /api/v1/auth/register - Creates a new account without generating a JWT
    @PostMapping("/register")
    public ResponseEntity<java.util.Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        java.util.Map<String, Object> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // POST /api/v1/auth/login - Authenticates an existing user
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
