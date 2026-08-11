package com.traveltrek.controller;

import com.traveltrek.entity.BookingStatus;
import com.traveltrek.repository.BookingReservationRepository;
import com.traveltrek.repository.TravelPackageRepository;
import com.traveltrek.repository.TripItineraryRepository;
import com.traveltrek.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ReportController
 *
 * Purpose: Provides the AGENCY_MANAGER reports/dashboard summary required by
 *          the RBAC spec ("View reports/dashboard").
 * Why it exists: Built entirely from existing repositories/aggregate counts,
 *                so no new entity or schema change is needed.
 * Access: AGENCY_MANAGER only (enforced in SecurityConfig via /api/v1/reports/**).
 */
@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:63342"
})
public class ReportController {

    private final UserRepository userRepository;
    private final TravelPackageRepository packageRepository;
    private final BookingReservationRepository bookingRepository;
    private final TripItineraryRepository itineraryRepository;

    public ReportController(UserRepository userRepository,
                             TravelPackageRepository packageRepository,
                             BookingReservationRepository bookingRepository,
                             TripItineraryRepository itineraryRepository) {
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
        this.bookingRepository = bookingRepository;
        this.itineraryRepository = itineraryRepository;
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("totalUsers", userRepository.count());
        summary.put("totalPackages", packageRepository.count());
        summary.put("totalItineraries", itineraryRepository.count());

        long totalBookings = bookingRepository.count();
        long confirmed = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED).count();
        long cancelled = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        long pending = totalBookings - confirmed - cancelled;

        summary.put("totalBookings", totalBookings);
        summary.put("confirmedBookings", confirmed);
        summary.put("cancelledBookings", cancelled);
        summary.put("pendingBookings", pending);

        return ResponseEntity.ok(summary);
    }
}
