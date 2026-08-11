package com.traveltrek.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "planned_activity")
public class PlannedActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String activityName;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    @JsonIgnore
    private TripItinerary itinerary;

    public PlannedActivity() {
    }

    public PlannedActivity(String activityName,
                           LocalDateTime scheduledAt,
                           BigDecimal estimatedCost,
                           TripItinerary itinerary) {
        this.activityName = activityName;
        this.scheduledAt = scheduledAt;
        this.estimatedCost = estimatedCost;
        this.itinerary = itinerary;
    }

    public Long getId() {
        return id;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public TripItinerary getItinerary() {
        return itinerary;
    }

    public void setItinerary(TripItinerary itinerary) {
        this.itinerary = itinerary;
    }
}