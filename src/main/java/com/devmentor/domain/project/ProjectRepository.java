package com.devmentor.domain.project;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for Project domain
 */
public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    List<Project> findByOwnerId(UUID ownerId);

    List<Project> findByOwnerIdAndStatus(UUID ownerId, ProjectStatus status);

    Optional<Project> findByGithubRepositoryUrl(String githubRepositoryUrl);

    void delete(Project project);

    long countByOwnerId(UUID ownerId);
}
