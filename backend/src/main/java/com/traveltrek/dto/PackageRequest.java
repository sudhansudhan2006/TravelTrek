package com.traveltrek.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * PackageRequest DTO
 *
 * Purpose: Captures input data when creating or updating a travel package.
 * Why it exists: Separates the API input contract from the TravelPackage entity.
 */
public class PackageRequest {

    @NotBlank(message = "Package name is required")
    private String packageName;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private BigDecimal basePrice;

    @NotNull(message = "Available slots is required")
    @PositiveOrZero(message = "Available slots cannot be negative")
    private Integer availableSlots;

    // --- Getters and Setters ---

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }
}
