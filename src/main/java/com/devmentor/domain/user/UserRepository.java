package com.devmentor.domain.user;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for User domain (Hexagonal Architecture)
 * This is the interface that defines what the domain needs from the infrastructure
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByGithubId(Long githubId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void delete(User user);
}
