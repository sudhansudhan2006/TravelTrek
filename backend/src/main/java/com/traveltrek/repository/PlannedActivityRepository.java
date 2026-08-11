package com.traveltrek.repository;

import com.traveltrek.entity.PlannedActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * PlannedActivityRepository
 *
 * Purpose: Handles all database operations for PlannedActivity entity.
 * Why it exists: Activities are always retrieved in context of an itinerary.
 * How it works:
 *   - findByItineraryId: fetches all activities for a given itinerary.
 *     Spring generates: SELECT * FROM planned_activity WHERE itinerary_id = ?
 */
@Repository
public interface PlannedActivityRepository extends JpaRepository<PlannedActivity, Long> {

    // Fetch all activities belonging to a specific itinerary
    List<PlannedActivity> findByItineraryId(Long itineraryId);
}
