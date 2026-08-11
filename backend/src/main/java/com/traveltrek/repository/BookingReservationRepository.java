package com.traveltrek.repository;

import com.traveltrek.entity.BookingReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingReservationRepository extends JpaRepository<BookingReservation, Long> {

    // Get all bookings for a user
    List<BookingReservation> findByUserId(Long userId);

    // Find booking by reference number
    Optional<BookingReservation> findByBookingReference(String bookingReference);
}