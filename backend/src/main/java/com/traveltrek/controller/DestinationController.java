package com.traveltrek.controller;

import com.traveltrek.repository.TravelPackageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * DestinationController
 *
 * Purpose: Gives the AGENCY_MANAGER a "Destinations" view (per the RBAC spec).
 * Why it exists: The current schema stores destination as a plain field on
 *                TravelPackage rather than a first-class Destination entity.
 *                Introducing a new entity would require a schema/migration
 *                change, which the project explicitly asks to avoid unless
 *                required. This controller exposes the distinct destination
 *                names already in the system without altering the database.
 * Access: AGENCY_MANAGER only (enforced in SecurityConfig via /api/v1/destinations/**).
 */
@RestController
@RequestMapping("/api/v1/destinations")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:63342",
        "https://traveltrek-dun.vercel.app"
})
public class DestinationController {

    private final TravelPackageRepository travelPackageRepository;

    public DestinationController(TravelPackageRepository travelPackageRepository) {
        this.travelPackageRepository = travelPackageRepository;
    }

    @GetMapping
    public ResponseEntity<List<String>> getDestinations() {
        return ResponseEntity.ok(travelPackageRepository.findDistinctDestinations());
    }
}
