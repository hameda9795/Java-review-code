package com.devmentor.infrastructure.persistence.jpa;

import com.devmentor.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity
 */
@Repository
public interface JpaUserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByGithubId(Long githubId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByIsSpecialUser(Boolean isSpecialUser);

    long countByIsActive(Boolean isActive);

    long countByIsSpecialUser(Boolean isSpecialUser);
}
