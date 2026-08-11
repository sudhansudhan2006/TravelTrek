package com.traveltrek.service;

import com.traveltrek.dto.PackageRequest;
import com.traveltrek.entity.TravelPackage;
import com.traveltrek.exception.BadRequestException;
import com.traveltrek.exception.ResourceNotFoundException;
import com.traveltrek.repository.TravelPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * PackageService
 *
 * Purpose: Contains all business logic related to travel packages.
 * Why it exists: Keeps the controller thin; all rules (e.g., slot management)
 *                live in one place.
 * How it works: Standard CRUD plus a method to decrement available slots on booking.
 */
@Service
public class PackageService {

    private static final Logger log = LoggerFactory.getLogger(PackageService.class);

    private final TravelPackageRepository packageRepository;

    public PackageService(TravelPackageRepository packageRepository) {
        this.packageRepository = packageRepository;
    }

    // Returns paginated packages, optionally filtered by destination
    public Page<TravelPackage> searchPackages(String destination, Pageable pageable) {
        if (destination == null || destination.isBlank()) {
            return packageRepository.findAll(pageable);
        }
        return packageRepository.findByDestinationContainingIgnoreCase(destination, pageable);
    }

    // Fetches a single package or throws 404 if not found
    public TravelPackage getPackageById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Package not found with id: " + id));
    }

    // Creates a new travel package (AGENCY_MANAGER only, enforced in SecurityConfig)
    public TravelPackage createPackage(PackageRequest request) {
        TravelPackage travelPackage = new TravelPackage(
                request.getPackageName(),
                request.getDestination(),
                request.getBasePrice(),
                request.getAvailableSlots()
        );
        TravelPackage saved = packageRepository.save(travelPackage);
        log.info("Package created: {} ({})", saved.getPackageName(), saved.getDestination());
        return saved;
    }

    // Updates an existing package's details
    public TravelPackage updatePackage(Long id, PackageRequest request) {
        TravelPackage travelPackage = getPackageById(id);
        travelPackage.setPackageName(request.getPackageName());
        travelPackage.setDestination(request.getDestination());
        travelPackage.setBasePrice(request.getBasePrice());
        travelPackage.setAvailableSlots(request.getAvailableSlots());
        log.info("Package updated: id={}", id);
        return packageRepository.save(travelPackage);
    }

    // Deletes a package by id
    public void deletePackage(Long id) {
        TravelPackage travelPackage = getPackageById(id);
        packageRepository.delete(travelPackage);
        log.info("Package deleted: id={}", id);
    }

    // Reduces available slots by 1 when a booking is confirmed; called by BookingService
    public void reserveSlot(TravelPackage travelPackage) {
        if (travelPackage.getAvailableSlots() <= 0) {
            throw new BadRequestException("No available slots for package: " + travelPackage.getPackageName());
        }
        travelPackage.setAvailableSlots(travelPackage.getAvailableSlots() - 1);
        packageRepository.save(travelPackage);
    }
}
