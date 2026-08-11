package com.traveltrek.dto;

import jakarta.validation.constraints.NotNull;

/**
 * StatusUpdateRequest DTO
 *
 * Purpose: Used by AGENCY_MANAGER to activate/deactivate a user account.
 */
public class StatusUpdateRequest {

    @NotNull(message = "active is required")
    private Boolean active;

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
