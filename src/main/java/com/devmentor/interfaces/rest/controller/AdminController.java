package com.devmentor.interfaces.rest.controller;

import com.devmentor.application.admin.AdminService;
import com.devmentor.interfaces.rest.dto.AdminStatsDTO;
import com.devmentor.interfaces.rest.dto.CreateSpecialUserRequest;
import com.devmentor.interfaces.rest.dto.UpdateUserRequest;
import com.devmentor.interfaces.rest.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Admin request: Get all users");
        List<UserDTO> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/special")
    public ResponseEntity<List<UserDTO>> getSpecialUsers() {
        log.info("Admin request: Get special users");
        List<UserDTO> specialUsers = adminService.getSpecialUsers();
        return ResponseEntity.ok(specialUsers);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        log.info("Admin request: Get user by id: {}", userId);
        UserDTO user = adminService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/special")
    public ResponseEntity<UserDTO> createSpecialUser(@Valid @RequestBody CreateSpecialUserRequest request) {
        log.info("Admin request: Create special user: {}", request.getUsername());
        UserDTO user = adminService.createSpecialUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("Admin request: Update user: {}", userId);
        UserDTO user = adminService.updateUser(userId, request);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        log.info("Admin request: Delete user: {}", userId);
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/reset-usage")
    public ResponseEntity<UserDTO> resetUserUsage(@PathVariable UUID userId) {
        log.info("Admin request: Reset usage for user: {}", userId);
        UserDTO user = adminService.resetUserUsage(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getAdminStats() {
        log.info("Admin request: Get admin statistics");
        AdminStatsDTO stats = adminService.getAdminStats();
        return ResponseEntity.ok(stats);
    }
}
