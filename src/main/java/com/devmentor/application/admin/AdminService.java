package com.devmentor.application.admin;

import com.devmentor.domain.codereview.CodeReviewRepository;
import com.devmentor.domain.user.User;
import com.devmentor.domain.user.UserRepository;
import com.devmentor.domain.user.UserRole;
import com.devmentor.interfaces.rest.dto.AdminStatsDTO;
import com.devmentor.interfaces.rest.dto.CreateSpecialUserRequest;
import com.devmentor.interfaces.rest.dto.UpdateUserRequest;
import com.devmentor.interfaces.rest.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CodeReviewRepository codeReviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getSpecialUsers() {
        return userRepository.findByIsSpecialUser(true).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return convertToDTO(user);
    }

    @Transactional
    public UserDTO createSpecialUser(CreateSpecialUserRequest request) {
        // Validate that username and email are not already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(UserRole.USER)
                .isActive(true)
                .isSpecialUser(true)
                .usageLimit(request.getUsageLimit())
                .emailVerified(true)
                .build();

        user = userRepository.save(user);
        log.info("Created special user: {} with usage limit: {}", user.getUsername(), user.getUsageLimit());

        return convertToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(UUID userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getSubscriptionTier() != null) {
            user.setSubscriptionTier(request.getSubscriptionTier());
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        if (request.getIsSpecialUser() != null) {
            if (request.getIsSpecialUser()) {
                user.makeSpecialUser(request.getUsageLimit());
            } else {
                user.revokeSpecialUser();
            }
        } else if (request.getUsageLimit() != null && user.getIsSpecialUser()) {
            user.updateUsageLimit(request.getUsageLimit());
        }

        user = userRepository.save(user);
        log.info("Updated user: {}", user.getUsername());

        return convertToDTO(user);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Prevent deletion of admin users
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("Cannot delete admin users");
        }

        userRepository.delete(user);
        log.info("Deleted user: {}", user.getUsername());
    }

    @Transactional
    public UserDTO resetUserUsage(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        user.setReviewsCount(0);
        user = userRepository.save(user);
        log.info("Reset usage for user: {}", user.getUsername());

        return convertToDTO(user);
    }

    @Transactional(readOnly = true)
    public AdminStatsDTO getAdminStats() {
        long totalUsers = userRepository.countAll();
        long activeUsers = userRepository.countByIsActive(true);
        long specialUsers = userRepository.countByIsSpecialUser(true);
        long totalReviews = codeReviewRepository.countAll();

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = LocalDateTime.now().minusWeeks(1);
        LocalDateTime startOfMonth = LocalDateTime.now().minusMonths(1);

        long reviewsToday = codeReviewRepository.countByCreatedAtAfter(startOfDay);
        long reviewsThisWeek = codeReviewRepository.countByCreatedAtAfter(startOfWeek);
        long reviewsThisMonth = codeReviewRepository.countByCreatedAtAfter(startOfMonth);

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .specialUsers(specialUsers)
                .totalReviews(totalReviews)
                .reviewsToday(reviewsToday)
                .reviewsThisWeek(reviewsThisWeek)
                .reviewsThisMonth(reviewsThisMonth)
                .build();
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .githubUsername(user.getGithubUsername())
                .role(user.getRole())
                .subscriptionTier(user.getSubscriptionTier())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .isSpecialUser(user.getIsSpecialUser())
                .usageLimit(user.getUsageLimit())
                .reviewsCount(user.getReviewsCount())
                .totalFilesReviewed(user.getTotalFilesReviewed())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
