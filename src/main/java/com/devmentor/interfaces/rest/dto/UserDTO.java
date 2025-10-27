package com.devmentor.interfaces.rest.dto;

import com.devmentor.domain.user.SubscriptionTier;
import com.devmentor.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String githubUsername;
    private UserRole role;
    private SubscriptionTier subscriptionTier;
    private Boolean isActive;
    private Boolean emailVerified;
    private Boolean isSpecialUser;
    private Integer usageLimit;
    private Integer reviewsCount;
    private Integer totalFilesReviewed;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
