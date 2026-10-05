package com.traveltrek.dto;

import com.traveltrek.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * RegisterRequest DTO
 *
 * Purpose: Captures the data needed for a new user to register.
 * Why it exists: We never expose the User entity directly in API requests —
 *                this keeps the API contract separate from the database structure
 *                and lets us validate input before it touches the entity.
 */
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Optional: any role provided by client is ignored by registration logic
    private UserRole role;

    // --- Getters and Setters ---

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
