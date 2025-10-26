package com.devmentor.infrastructure.github;

import lombok.Builder;
import lombok.Data;

/**
 * GitHub repository information
 */
@Data
@Builder
public class GitHubRepository {
    private Long id;
    private String name;
    private String fullName;
    private String description;
    private String url;
    private String language;
    private String defaultBranch;
    private Boolean isPrivate;
}
