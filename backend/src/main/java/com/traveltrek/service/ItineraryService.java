package com.traveltrek.service;

import com.traveltrek.dto.ItineraryRequest;
import com.traveltrek.entity.TripItinerary;
import com.traveltrek.entity.User;
import com.traveltrek.entity.UserRole;
import com.traveltrek.exception.ResourceNotFoundException;
import com.traveltrek.repository.TripItineraryRepository;
import com.traveltrek.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItineraryService {

    private static final Logger log =
            LoggerFactory.getLogger(ItineraryService.class);

    private final TripItineraryRepository itineraryRepository;
    private final UserRepository userRepository;

    public ItineraryService(
            TripItineraryRepository itineraryRepository,
            UserRepository userRepository) {

        this.itineraryRepository = itineraryRepository;
        this.userRepository = userRepository;
    }

    public TripItinerary createItinerary(
            ItineraryRequest request,
            String userEmail) {

        log.info("Creating itinerary for user: {}", userEmail);

        User user = findUserByEmail(userEmail);

        TripItinerary itinerary = new TripItinerary(
                request.getTitle(),
                request.getDestination(),
                request.getTargetBudget(),
                false,
                user
        );

        TripItinerary saved = itineraryRepository.save(itinerary);

        log.info(
                "Itinerary created successfully. ID={}, Title={}",
                saved.getId(),
                saved.getTitle()
        );

        return saved;
    }

    public List<TripItinerary> getMyItineraries(String userEmail) {

        log.info("Fetching itineraries for user: {}", userEmail);

        User user = findUserByEmail(userEmail);

        List<TripItinerary> itineraries =
                itineraryRepository.findByUserId(user.getId());

        log.info(
                "Found {} itineraries for user {}",
                itineraries.size(),
                userEmail
        );

        return itineraries;
    }

    // Returns every itinerary in the system (TRAVEL_AGENT / AGENCY_MANAGER only,
    // enforced at the URL level in SecurityConfig)
    public List<TripItinerary> getAllItineraries() {
        log.info("Fetching all itineraries (agent/manager view)");
        return itineraryRepository.findAll();
    }

    // Fetches a single itinerary, enforcing ownership for TRAVELERs:
    // a traveler may only view their own itinerary; agents/managers may view any.
    public TripItinerary getItineraryById(Long id, String requesterEmail) {

        log.info("Fetching itinerary ID={}", id);

        TripItinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Itinerary not found with id: " + id));

        assertCanAccess(itinerary, requesterEmail);
        return itinerary;
    }

    // Updates an itinerary. Ownership rule: a TRAVELER may edit ONLY their own
    // itinerary; AGENCY_MANAGER has full access.
    public TripItinerary updateItinerary(Long id, ItineraryRequest request, String requesterEmail) {

        TripItinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Itinerary not found with id: " + id));

        assertCanModify(itinerary, requesterEmail);

        itinerary.setTitle(request.getTitle());
        itinerary.setDestination(request.getDestination());
        itinerary.setTargetBudget(request.getTargetBudget());

        TripItinerary saved = itineraryRepository.save(itinerary);
        log.info("Itinerary updated: id={} by {}", id, requesterEmail);
        return saved;
    }

    // Deletes an itinerary. Ownership rule: a TRAVELER may delete ONLY their own
    // itinerary; AGENCY_MANAGER has full access.
    public void deleteItinerary(Long id, String requesterEmail) {

        TripItinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Itinerary not found with id: " + id));

        assertCanModify(itinerary, requesterEmail);

        itineraryRepository.delete(itinerary);
        log.info("Itinerary deleted: id={} by {}", id, requesterEmail);
    }

    // Read access: owner, TRAVEL_AGENT, or AGENCY_MANAGER
    private void assertCanAccess(TripItinerary itinerary, String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);
        boolean isOwner = itinerary.getUser() != null
                && itinerary.getUser().getId().equals(requester.getId());
        boolean isStaff = requester.getRole() == UserRole.TRAVEL_AGENT
                || requester.getRole() == UserRole.AGENCY_MANAGER;

        if (!isOwner && !isStaff) {
            throw new AccessDeniedException("You can only view your own itineraries.");
        }
    }

    // Write access (edit/delete): owner (if TRAVELER) or AGENCY_MANAGER only.
    // TRAVEL_AGENT is explicitly excluded - agents may view all itineraries but
    // must not modify a traveler's itinerary.
    private void assertCanModify(TripItinerary itinerary, String requesterEmail) {
        User requester = findUserByEmail(requesterEmail);
        boolean isOwner = itinerary.getUser() != null
                && itinerary.getUser().getId().equals(requester.getId());
        boolean isManager = requester.getRole() == UserRole.AGENCY_MANAGER;

        if (!isOwner && !isManager) {
            throw new AccessDeniedException("You can only modify your own itineraries.");
        }
    }

    private User findUserByEmail(String email) {

        log.info("Searching user with email: {}", email);

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + email));
    }
}
