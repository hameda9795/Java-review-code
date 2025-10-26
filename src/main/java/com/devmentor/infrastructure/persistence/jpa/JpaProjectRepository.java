package com.devmentor.infrastructure.persistence.jpa;

import com.devmentor.domain.project.Project;
import com.devmentor.domain.project.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Project entity
 */
@Repository
public interface JpaProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByOwnerId(UUID ownerId);

    List<Project> findByOwnerIdAndStatus(UUID ownerId, ProjectStatus status);

    Optional<Project> findByGithubRepositoryUrl(String githubRepositoryUrl);

    long countByOwnerId(UUID ownerId);
}
