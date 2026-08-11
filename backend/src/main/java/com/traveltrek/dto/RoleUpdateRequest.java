package com.traveltrek.dto;

import com.traveltrek.entity.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * RoleUpdateRequest DTO
 *
 * Purpose: Used by AGENCY_MANAGER to change another user's role.
 */
public class RoleUpdateRequest {

    @NotNull(message = "Role is required")
    private UserRole role;

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
