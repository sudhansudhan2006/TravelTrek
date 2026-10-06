package com.traveltrek.controller;

import com.traveltrek.dto.BookingRequest;
import com.traveltrek.entity.BookingReservation;
import com.traveltrek.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:63342",
        "https://traveltrek-dun.vercel.app"
})
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Create a booking
    @PostMapping
    public ResponseEntity<?> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        BookingReservation booking = bookingService.createBooking(request, userEmail);

        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    // Get all bookings of the logged-in user
    @GetMapping("/my")
    public ResponseEntity<?> getMyBookings(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not authenticated.");
        }

        String userEmail = authentication.getName();

        List<BookingReservation> bookings = bookingService.getMyBookings(userEmail);

        return ResponseEntity.ok(bookings);
    }

    // Get every booking in the system (TRAVEL_AGENT / AGENCY_MANAGER only - see SecurityConfig)
    @GetMapping
    public ResponseEntity<?> getAllBookings(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not authenticated.");
        }

        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    // Cancel booking - ownership is validated inside BookingService
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User is not authenticated.");
        }

        BookingReservation booking = bookingService.cancelBooking(id, authentication.getName());

        return ResponseEntity.ok(booking);
    }
}