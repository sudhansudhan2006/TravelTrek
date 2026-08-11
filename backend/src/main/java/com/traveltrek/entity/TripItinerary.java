package com.traveltrek.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trip_itinerary")
public class TripItinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false)
    private String destination;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal targetBudget;

    @Column(nullable = false)
    private boolean isAiGenerated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash"})
    private User user;

    @OneToMany(mappedBy = "itinerary",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonIgnore
    private List<PlannedActivity> activities = new ArrayList<>();

    public TripItinerary() {
    }

    public TripItinerary(String title,
                         String destination,
                         BigDecimal targetBudget,
                         boolean isAiGenerated,
                         User user) {
        this.title = title;
        this.destination = destination;
        this.targetBudget = targetBudget;
        this.isAiGenerated = isAiGenerated;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public BigDecimal getTargetBudget() {
        return targetBudget;
    }

    public void setTargetBudget(BigDecimal targetBudget) {
        this.targetBudget = targetBudget;
    }

    public boolean isAiGenerated() {
        return isAiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        isAiGenerated = aiGenerated;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<PlannedActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<PlannedActivity> activities) {
        this.activities = activities;
    }
}