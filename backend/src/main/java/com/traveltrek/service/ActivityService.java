package com.traveltrek.service;

import com.traveltrek.dto.ActivityRequest;
import com.traveltrek.entity.PlannedActivity;
import com.traveltrek.entity.TripItinerary;
import com.traveltrek.exception.ResourceNotFoundException;
import com.traveltrek.repository.PlannedActivityRepository;
import com.traveltrek.repository.TripItineraryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ActivityService
 *
 * Purpose: Contains business logic for adding and retrieving planned activities.
 * Why it exists: Keeps the controller thin; centralizes activity-related rules.
 * How it works: An activity always belongs to an existing itinerary, looked up by id.
 */
@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private final PlannedActivityRepository activityRepository;
    private final TripItineraryRepository itineraryRepository;

    public ActivityService(PlannedActivityRepository activityRepository, TripItineraryRepository itineraryRepository) {
        this.activityRepository = activityRepository;
        this.itineraryRepository = itineraryRepository;
    }

    // Adds a new activity to an existing itinerary
    public PlannedActivity addActivity(ActivityRequest request) {
        TripItinerary itinerary = itineraryRepository.findById(request.getItineraryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Itinerary not found with id: " + request.getItineraryId()));

        PlannedActivity activity = new PlannedActivity(
                request.getActivityName(),
                request.getScheduledAt(),
                request.getEstimatedCost(),
                itinerary
        );

        PlannedActivity saved = activityRepository.save(activity);
        log.info("Activity '{}' added to itinerary id={}", saved.getActivityName(), itinerary.getId());
        return saved;
    }

    // Updates an existing activity. Only TRAVEL_AGENT / AGENCY_MANAGER may call
    // this (enforced in SecurityConfig); there is no per-agent ownership concept
    // for activities, since any agent may assist any traveler.
    public PlannedActivity updateActivity(Long activityId, ActivityRequest request) {
        PlannedActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activity not found with id: " + activityId));

        TripItinerary itinerary = itineraryRepository.findById(request.getItineraryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Itinerary not found with id: " + request.getItineraryId()));

        activity.setActivityName(request.getActivityName());
        activity.setScheduledAt(request.getScheduledAt());
        activity.setEstimatedCost(request.getEstimatedCost());
        activity.setItinerary(itinerary);

        PlannedActivity saved = activityRepository.save(activity);
        log.info("Activity updated: id={}", activityId);
        return saved;
    }

    // Deletes an activity. Only TRAVEL_AGENT / AGENCY_MANAGER may call this
    // (enforced in SecurityConfig).
    public void deleteActivity(Long activityId) {
        PlannedActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Activity not found with id: " + activityId));
        activityRepository.delete(activity);
        log.info("Activity deleted: id={}", activityId);
    }

    // Returns all activities for a given itinerary
    public List<PlannedActivity> getActivitiesForItinerary(Long itineraryId) {
        return activityRepository.findByItineraryId(itineraryId);
    }
}
