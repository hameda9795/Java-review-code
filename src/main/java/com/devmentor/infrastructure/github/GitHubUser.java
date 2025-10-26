package com.devmentor.infrastructure.github;

import lombok.Builder;
import lombok.Data;

/**
 * GitHub user information
 */
@Data
@Builder
public class GitHubUser {
    private Long id;
    private String login;
    private String name;
    private String email;
    private String avatarUrl;
    private String profileUrl;
}
