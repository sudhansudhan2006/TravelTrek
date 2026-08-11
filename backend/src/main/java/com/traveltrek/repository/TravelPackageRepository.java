package com.traveltrek.repository;

import com.traveltrek.entity.TravelPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * TravelPackageRepository
 *
 * Purpose: Handles all database operations for TravelPackage entity.
 * Why it exists: Provides search functionality across packages by destination name.
 * How it works:
 *   - JpaRepository gives us findAll(), findById(), save(), delete() for free.
 *   - findByDestinationContainingIgnoreCase: Spring generates a LIKE query.
 *     Example: search "goa" → finds "Goa Beach", "North Goa", etc.
 *   - Pageable: supports pagination so we don't load all packages at once.
 */
@Repository
public interface TravelPackageRepository extends JpaRepository<TravelPackage, Long> {

    // Searches packages by destination (case-insensitive partial match)
    Page<TravelPackage> findByDestinationContainingIgnoreCase(String destination, Pageable pageable);

    // Distinct destination names, used by the AGENCY_MANAGER "Destinations" view.
    // Destinations are a field on TravelPackage rather than a separate entity,
    // so this avoids any database schema change.
    @Query("select distinct p.destination from TravelPackage p order by p.destination")
    List<String> findDistinctDestinations();
}
