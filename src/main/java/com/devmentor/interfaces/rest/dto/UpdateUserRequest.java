package com.devmentor.interfaces.rest.dto;

import com.devmentor.domain.user.SubscriptionTier;
import com.devmentor.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    private UserRole role;
    private SubscriptionTier subscriptionTier;
    private Boolean isActive;
    private Boolean isSpecialUser;
    private Integer usageLimit;
}
