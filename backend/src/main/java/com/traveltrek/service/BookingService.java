package com.traveltrek.service;

import com.traveltrek.dto.BookingRequest;
import com.traveltrek.entity.BookingReservation;
import com.traveltrek.entity.BookingStatus;
import com.traveltrek.entity.TravelPackage;
import com.traveltrek.entity.User;
import com.traveltrek.entity.UserRole;
import com.traveltrek.exception.BadRequestException;
import com.traveltrek.exception.ResourceNotFoundException;
import com.traveltrek.repository.BookingReservationRepository;
import com.traveltrek.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * BookingService
 *
 * Purpose: Contains business logic for booking travel packages.
 * Why it exists: Centralizes the booking workflow: validate slots, generate a
 *                reference number, decrement package slots, and save the booking.
 * How it works:
 *   1. Looks up the package and the logged-in user.
 *   2. Asks PackageService to reserve a slot (throws if none available).
 *   3. Generates a unique booking reference and saves the booking as CONFIRMED.
 */
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingReservationRepository bookingRepository;
    private final UserRepository userRepository;
    private final PackageService packageService;

    public BookingService(BookingReservationRepository bookingRepository,
                           UserRepository userRepository,
                           PackageService packageService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.packageService = packageService;
    }

    // Creates a new booking for the logged-in user
    public BookingReservation createBooking(BookingRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        TravelPackage travelPackage = packageService.getPackageById(request.getPackageId());

        // Reserve a slot; throws BadRequestException if none are available
        packageService.reserveSlot(travelPackage);

        String reference = generateBookingReference();
        BookingReservation booking = new BookingReservation(reference, travelPackage, user);
        booking.setStatus(BookingStatus.CONFIRMED);

        BookingReservation saved = bookingRepository.save(booking);
        log.info("Booking confirmed: {} for user {}", reference, userEmail);
        return saved;
    }

    // Returns all bookings made by the logged-in user
    public List<BookingReservation> getMyBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        return bookingRepository.findByUserId(user.getId());
    }

    // Returns every booking in the system (TRAVEL_AGENT / AGENCY_MANAGER only,
    // enforced at the URL level in SecurityConfig)
    public List<BookingReservation> getAllBookings() {
        return bookingRepository.findAll();
    }

    // Cancels an existing booking.
    // Ownership rule: a TRAVELER may cancel ONLY their own booking.
    // AGENCY_MANAGER has full access and may cancel any booking.
    public BookingReservation cancelBooking(Long bookingId, String requesterEmail) {
        BookingReservation booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requesterEmail));

        boolean isOwner = booking.getUser() != null
                && booking.getUser().getId().equals(requester.getId());
        boolean isManager = requester.getRole() == UserRole.AGENCY_MANAGER;

        if (!isOwner && !isManager) {
            throw new AccessDeniedException("You can only cancel your own bookings.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        log.info("Booking cancelled: {} by {}", booking.getBookingReference(), requesterEmail);
        return bookingRepository.save(booking);
    }

    // Generates a unique, human-readable booking reference like "TT-7F3A2B1C"
    private String generateBookingReference() {
        return "TT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
