package com.traveltrek.controller;

import com.traveltrek.dto.ItineraryRequest;
import com.traveltrek.entity.TripItinerary;
import com.traveltrek.service.ItineraryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/itineraries")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:63342",
        "https://traveltrek-dun.vercel.app"
})
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    // Only TRAVELERs may create itineraries (enforced in SecurityConfig)
    @PostMapping
    public ResponseEntity<TripItinerary> createItinerary(
            @Valid @RequestBody ItineraryRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();

        TripItinerary created =
                itineraryService.createItinerary(request, userEmail);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    // The logged-in user's own itineraries
    @GetMapping("/my")
    public ResponseEntity<List<TripItinerary>> getMyItineraries(
            Authentication authentication) {

        String userEmail = authentication.getName();

        List<TripItinerary> itineraries =
                itineraryService.getMyItineraries(userEmail);

        return ResponseEntity.ok(itineraries);
    }

    // Every itinerary in the system - TRAVEL_AGENT / AGENCY_MANAGER only (see SecurityConfig)
    @GetMapping("/all")
    public ResponseEntity<List<TripItinerary>> getAllItineraries() {
        return ResponseEntity.ok(itineraryService.getAllItineraries());
    }

    // Single itinerary - ownership enforced inside ItineraryService
    @GetMapping("/{id}")
    public ResponseEntity<TripItinerary> getItinerary(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                itineraryService.getItineraryById(id, authentication.getName())
        );
    }

    // Update an itinerary - a TRAVELER may edit only their own; AGENCY_MANAGER may edit any
    @PutMapping("/{id}")
    public ResponseEntity<TripItinerary> updateItinerary(
            @PathVariable Long id,
            @Valid @RequestBody ItineraryRequest request,
            Authentication authentication) {

        TripItinerary updated =
                itineraryService.updateItinerary(id, request, authentication.getName());

        return ResponseEntity.ok(updated);
    }

    // Delete an itinerary - a TRAVELER may delete only their own; AGENCY_MANAGER may delete any
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItinerary(
            @PathVariable Long id,
            Authentication authentication) {

        itineraryService.deleteItinerary(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
