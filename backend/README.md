# TravelTrek – Refactored (Simplified, Clean, Viva-Ready)

A Spring Boot 3.2 / Java 17 backend for an AI-based travel planning and booking system,
refactored for clarity while keeping real-world architecture (JWT auth, role-based
authorization, JPA relationships, layered design).

## Project Structure

```
src/main/java/com/traveltrek/
├── TravelTrekApplication.java       Main entry point
├── entity/                          JPA entities (database tables)
│   ├── User.java                    Renamed from SystemAccount
│   ├── UserRole.java                TRAVELER, TRAVEL_AGENT, AGENCY_MANAGER
│   ├── TravelPackage.java
│   ├── TripItinerary.java
│   ├── PlannedActivity.java
│   ├── BookingReservation.java
│   └── BookingStatus.java           PENDING, CONFIRMED, CANCELLED
├── repository/                      Spring Data JPA interfaces
│   ├── UserRepository.java
│   ├── TravelPackageRepository.java
│   ├── TripItineraryRepository.java
│   ├── PlannedActivityRepository.java
│   └── BookingReservationRepository.java
├── service/                         Business logic
│   ├── AuthService.java             Register + Login
│   ├── PackageService.java
│   ├── ItineraryService.java
│   ├── ActivityService.java
│   └── BookingService.java
├── controller/                      REST endpoints (thin, delegate to services)
│   ├── AuthController.java
│   ├── PackageController.java
│   ├── ItineraryController.java
│   ├── ActivityController.java
│   └── BookingController.java
├── dto/                             Request/response objects (only where useful)
│   ├── RegisterRequest.java / LoginRequest.java / AuthResponse.java
│   ├── PackageRequest.java
│   ├── ItineraryRequest.java
│   ├── ActivityRequest.java
│   └── BookingRequest.java
├── security/                        JWT machinery
│   ├── JwtService.java              Generate/validate tokens
│   └── JwtAuthFilter.java           Runs once per request
├── config/
│   └── SecurityConfig.java          Single security configuration class
└── exception/
    ├── ResourceNotFoundException.java
    ├── BadRequestException.java
    └── GlobalExceptionHandler.java  ONE handler for all exceptions
```

## Why this is simpler than a typical over-engineered version

- **One security config, one JWT filter, one JWT service** — no extra custom
  authentication providers, entry points, or manager configs.
- **One GlobalExceptionHandler** — every error returns a consistent JSON shape.
- **DTOs only where the API contract differs from the entity** (auth, create/update
  requests) — not one for every read operation.
- **No Lombok** — every getter/setter is visible and explainable line-by-line in a viva.
- **Plain JpaRepository method-naming queries** (`findByEmail`, `findByUserId`) —
  no custom `@Query` unless Spring's naming convention can't express it.
- **Lazy loading everywhere** on `@ManyToOne` to avoid N+1 / over-fetching, and
  **pagination** (`Pageable`) on the package search endpoint.

## How Authentication Works (for viva explanation)

1. `POST /api/v1/auth/register` → password is hashed with BCrypt, user saved, JWT issued.
2. `POST /api/v1/auth/login` → password checked against hash, JWT issued.
3. Every other request must include `Authorization: Bearer <token>`.
4. `JwtAuthFilter` reads the token once per request, validates it, and tells Spring
   Security "this user, with this role, is authenticated" — no server-side session.
5. `SecurityConfig` decides which roles can hit which endpoints
   (e.g., only `AGENCY_MANAGER` can create packages).

## Entity Relationships

- `User` 1 → many `TripItinerary`
- `TripItinerary` 1 → many `PlannedActivity` (cascade delete)
- `User` 1 → many `BookingReservation`
- `TravelPackage` 1 → many `BookingReservation`

## Running the Project

1. Create a MySQL database (or let `createDatabaseIfNotExist=true` do it).
2. Update `src/main/resources/application.properties` with your DB credentials.
3. `mvn spring-boot:run`
4. API base URL: `http://localhost:8080/api/v1`

## Sample API Flow

```
POST /api/v1/auth/register   { email, password, role }       → returns JWT
POST /api/v1/auth/login      { email, password }              → returns JWT
GET  /api/v1/packages                                         (any logged-in user)
POST /api/v1/packages        (AGENCY_MANAGER only)
POST /api/v1/itineraries     { title, destination, targetBudget }
POST /api/v1/activities      { activityName, scheduledAt, estimatedCost, itineraryId }
POST /api/v1/bookings        { packageId }
```
