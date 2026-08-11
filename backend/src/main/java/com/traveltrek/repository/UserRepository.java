package com.traveltrek.repository;

import com.traveltrek.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * UserRepository
 *
 * Purpose: Handles all database operations for the User entity.
 * Why it exists: Spring Data JPA automatically implements CRUD methods.
 *                We only add custom methods when Spring's method naming convention isn't enough.
 * How it works: Spring generates SQL queries from method names automatically.
 *   - findByEmail → SELECT * FROM system_accounts WHERE email = ?
 *   - existsByEmail → SELECT COUNT(*) FROM system_accounts WHERE email = ?
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used during login to find user by their email address
    Optional<User> findByEmail(String email);

    // Used during registration to check if email is already taken
    boolean existsByEmail(String email);
}
