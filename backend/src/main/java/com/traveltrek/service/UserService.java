package com.traveltrek.service;

import com.traveltrek.entity.User;
import com.traveltrek.entity.UserRole;
import com.traveltrek.exception.ResourceNotFoundException;
import com.traveltrek.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserService
 *
 * Purpose: Backs the AGENCY_MANAGER-only "Manage users" feature from the RBAC
 *          spec. Uses the existing User entity/table - no schema change.
 * Access:  Every method here is only reachable through UserController, which
 *          is restricted to AGENCY_MANAGER in SecurityConfig.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setRole(newRole);
        log.info("User {} role changed to {}", user.getEmail(), newRole);
        return userRepository.save(user);
    }

    public User updateActiveStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setActive(active);
        log.info("User {} active status set to {}", user.getEmail(), active);
        return userRepository.save(user);
    }
}
