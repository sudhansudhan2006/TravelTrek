package com.traveltrek.controller;

import com.traveltrek.dto.RoleUpdateRequest;
import com.traveltrek.dto.StatusUpdateRequest;
import com.traveltrek.entity.User;
import com.traveltrek.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UserController
 *
 * Purpose: "Manage users" for AGENCY_MANAGER (per RBAC spec). All endpoints
 *          here are restricted to AGENCY_MANAGER at the URL level in
 *          SecurityConfig (/api/v1/users/**), so no role checks are needed here.
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500",
        "http://localhost:63342",
        "https://traveltrek-dun.vercel.app"
})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<User> updateRole(@PathVariable Long id,
                                            @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request.getRole()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<User> updateStatus(@PathVariable Long id,
                                              @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(userService.updateActiveStatus(id, request.getActive()));
    }
}
