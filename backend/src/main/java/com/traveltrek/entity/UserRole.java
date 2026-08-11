package com.traveltrek.entity;

/**
 * UserRole Enum
 *
 * Purpose: Defines the three roles available in the TravelTrek system.
 * Why it exists: Spring Security uses roles to control which endpoints each user can access.
 * How it works: Stored as a String in the database (e.g., "TRAVELER") via @Enumerated(EnumType.STRING).
 */
public enum UserRole {
    TRAVELER,       // Regular user: browses packages, creates itineraries, makes bookings
    TRAVEL_AGENT,   // Agent: adds activities to itineraries
    AGENCY_MANAGER  // Manager: full access, manages packages
}
