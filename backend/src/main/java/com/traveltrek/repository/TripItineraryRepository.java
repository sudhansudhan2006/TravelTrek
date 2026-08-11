package com.traveltrek.repository;

import com.traveltrek.entity.TripItinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * TripItineraryRepository
 *
 * Purpose: Handles all database operations for TripItinerary entity.
 * Why it exists: Travelers need to see only their own itineraries, not everyone's.
 * How it works:
 *   - findByUserId: fetches all itineraries belonging to a specific user.
 *     Spring generates: SELECT * FROM trip_itinerary WHERE user_id = ?
 */
@Repository
public interface TripItineraryRepository extends JpaRepository<TripItinerary, Long> {

    // Fetch all itineraries created by a specific user
    List<TripItinerary> findByUserId(Long userId);
}
