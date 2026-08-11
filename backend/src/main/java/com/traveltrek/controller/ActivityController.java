package com.traveltrek.controller;

import com.traveltrek.dto.ActivityRequest;
import com.traveltrek.entity.PlannedActivity;
import com.traveltrek.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/activities")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:63342"
})
public class ActivityController {
    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // TRAVEL_AGENT or AGENCY_MANAGER only (enforced in SecurityConfig)
    @PostMapping
    public ResponseEntity<PlannedActivity> addActivity(@Valid @RequestBody ActivityRequest request) {
        PlannedActivity created = activityService.addActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Any authenticated role may view planned activities for an itinerary
    @GetMapping("/itinerary/{itineraryId}")
    public ResponseEntity<List<PlannedActivity>> getActivities(@PathVariable Long itineraryId) {
        return ResponseEntity.ok(activityService.getActivitiesForItinerary(itineraryId));
    }

    // TRAVEL_AGENT or AGENCY_MANAGER only (enforced in SecurityConfig)
    @PutMapping("/{id}")
    public ResponseEntity<PlannedActivity> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityRequest request) {
        return ResponseEntity.ok(activityService.updateActivity(id, request));
    }

    // TRAVEL_AGENT or AGENCY_MANAGER only (enforced in SecurityConfig)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
}
