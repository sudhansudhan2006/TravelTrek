package com.traveltrek.controller;

import com.traveltrek.dto.ProfileUpdateRequest;
import com.traveltrek.entity.User;
import com.traveltrek.exception.BadRequestException;
import com.traveltrek.exception.ResourceNotFoundException;
import com.traveltrek.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * ProfileController
 *
 * Purpose: Lets EVERY authenticated role (TRAVELER, TRAVEL_AGENT,
 *          AGENCY_MANAGER) view and update only their OWN account -
 *          "Update only their own profile" in the RBAC spec.
 * Why separate from UserController: UserController (/api/v1/users/**) is
 *          reserved for AGENCY_MANAGER managing OTHER users. This endpoint
 *          intentionally never accepts a user id/role - it always resolves
 *          the target user from the JWT (Authentication.getName()), so there
 *          is no way for a traveler/agent to touch another account.
 */
@RestController
@RequestMapping("/api/v1/profile")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:63342",
        "https://traveltrek-dun.vercel.app"
})
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(Authentication authentication) {
        User user = findByEmail(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {

        User user = findByEmail(authentication.getName());

        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getNewPassword().length() < 6) {
                throw new BadRequestException("Password must be at least 6 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        return ResponseEntity.ok(userRepository.save(user));
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
