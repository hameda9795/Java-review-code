package com.devmentor.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    @Builder.Default
    private String type = "Bearer";
    private UUID userId;
    private String username;
    private String email;
    private String subscriptionTier;
    private String githubUsername;
    private Boolean githubConnected;
}
