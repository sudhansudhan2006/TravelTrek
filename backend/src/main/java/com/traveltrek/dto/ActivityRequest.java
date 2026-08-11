package com.traveltrek.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ActivityRequest DTO
 *
 * Purpose: Captures input data when a travel agent adds an activity to an itinerary.
 * Why it exists: Keeps the API input contract separate from the PlannedActivity entity.
 */
public class ActivityRequest {

    @NotBlank(message = "Activity name is required")
    private String activityName;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    @NotNull(message = "Estimated cost is required")
    @Positive(message = "Estimated cost must be positive")
    private BigDecimal estimatedCost;

    @NotNull(message = "Itinerary id is required")
    private Long itineraryId;

    // --- Getters and Setters ---

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost; }

    public Long getItineraryId() { return itineraryId; }
    public void setItineraryId(Long itineraryId) { this.itineraryId = itineraryId; }
}
