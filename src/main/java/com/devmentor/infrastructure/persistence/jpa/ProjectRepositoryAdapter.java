package com.devmentor.infrastructure.persistence.jpa;

import com.devmentor.domain.project.Project;
import com.devmentor.domain.project.ProjectRepository;
import com.devmentor.domain.project.ProjectStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for Project repository
 */
@Component
@RequiredArgsConstructor
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final JpaProjectRepository jpaProjectRepository;

    @Override
    public Project save(Project project) {
        return jpaProjectRepository.save(project);
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return jpaProjectRepository.findById(id);
    }

    @Override
    public List<Project> findByOwnerId(UUID ownerId) {
        return jpaProjectRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Project> findByOwnerIdAndStatus(UUID ownerId, ProjectStatus status) {
        return jpaProjectRepository.findByOwnerIdAndStatus(ownerId, status);
    }

    @Override
    public Optional<Project> findByGithubRepositoryUrl(String githubRepositoryUrl) {
        return jpaProjectRepository.findByGithubRepositoryUrl(githubRepositoryUrl);
    }

    @Override
    public void delete(Project project) {
        jpaProjectRepository.delete(project);
    }

    @Override
    public long countByOwnerId(UUID ownerId) {
        return jpaProjectRepository.countByOwnerId(ownerId);
    }
}
