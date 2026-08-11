package com.traveltrek.dto;

import jakarta.validation.constraints.NotNull;

/**
 * BookingRequest DTO
 *
 * Purpose: Captures input data when a traveler books a travel package.
 * Why it exists: Keeps the API input contract minimal — the user is identified
 *                from the JWT token, not from the request body.
 */
public class BookingRequest {

    @NotNull(message = "Package id is required")
    private Long packageId;

    // --- Getters and Setters ---

    public Long getPackageId() { return packageId; }
    public void setPackageId(Long packageId) { this.packageId = packageId; }
}
