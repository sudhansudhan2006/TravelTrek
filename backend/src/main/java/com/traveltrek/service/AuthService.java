package com.traveltrek.service;

import com.traveltrek.dto.AuthResponse;
import com.traveltrek.dto.LoginRequest;
import com.traveltrek.dto.RegisterRequest;
import com.traveltrek.entity.User;
import com.traveltrek.exception.BadRequestException;
import com.traveltrek.repository.UserRepository;
import com.traveltrek.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService
 *
 * Purpose: Handles user registration and login business logic.
 * Why it exists: Keeps authentication logic out of the controller, so the
 *                controller only handles HTTP request/response work.
 * How it works:
 *   - register(): checks email isn't taken, hashes the password, saves the user.
 *   - login(): verifies email/password match, then issues a JWT token.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Registers a new user and immediately logs them in (returns a token)
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()), // Never store plain text passwords
                request.getRole()
        );
        userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    // Authenticates an existing user and returns a new token
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }
}
