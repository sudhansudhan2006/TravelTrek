package com.traveltrek.dto;

import com.traveltrek.entity.UserRole;

/**
 * AuthResponse DTO
 *
 * Purpose: Sent back to the client after successful login or registration.
 * Why it exists: Bundles the JWT token with basic user info so the frontend
 *                knows who is logged in and what role they have.
 */
public class AuthResponse {

    private String token;
    private String email;
    private UserRole role;

    public AuthResponse(String token, String email, UserRole role) {
        this.token = token;
        this.email = email;
        this.role = role;
    }

    // --- Getters ---

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
}
