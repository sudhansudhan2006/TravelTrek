package com.traveltrek.dto;

import jakarta.validation.constraints.Email;

/**
 * ProfileUpdateRequest DTO
 *
 * Purpose: Used by the logged-in user to update their OWN profile
 * (email and/or password). Role cannot be changed here - only
 * AGENCY_MANAGER (via UserController) can change roles.
 */
public class ProfileUpdateRequest {

    @Email(message = "A valid email is required")
    private String email;

    // Optional - only updated if provided (min 6 chars, same rule as registration)
    private String newPassword;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
