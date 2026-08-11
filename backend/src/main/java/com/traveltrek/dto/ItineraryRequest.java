package com.traveltrek.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * ItineraryRequest DTO
 *
 * Purpose: Captures input data when a traveler creates a new trip itinerary.
 * Why it exists: Keeps the API input contract separate from the TripItinerary entity.
 */
public class ItineraryRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Target budget is required")
    @Positive(message = "Target budget must be positive")
    private BigDecimal targetBudget;

    // --- Getters and Setters ---

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public BigDecimal getTargetBudget() { return targetBudget; }
    public void setTargetBudget(BigDecimal targetBudget) { this.targetBudget = targetBudget; }
}
