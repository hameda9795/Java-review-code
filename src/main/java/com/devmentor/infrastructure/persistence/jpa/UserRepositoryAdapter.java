package com.devmentor.infrastructure.persistence.jpa;

import com.devmentor.domain.user.User;
import com.devmentor.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that bridges domain UserRepository interface with Spring Data JPA implementation
 * This follows the Hexagonal Architecture pattern (Ports & Adapters)
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaUserRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByGithubId(Long githubId) {
        return jpaUserRepository.findByGithubId(githubId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaUserRepository.existsByUsername(username);
    }

    @Override
    public void delete(User user) {
        jpaUserRepository.delete(user);
    }

    @Override
    public List<User> findAll() {
        return jpaUserRepository.findAll();
    }

    @Override
    public List<User> findByIsSpecialUser(Boolean isSpecialUser) {
        return jpaUserRepository.findByIsSpecialUser(isSpecialUser);
    }

    @Override
    public long countAll() {
        return jpaUserRepository.count();
    }

    @Override
    public long countByIsActive(Boolean isActive) {
        return jpaUserRepository.countByIsActive(isActive);
    }

    @Override
    public long countByIsSpecialUser(Boolean isSpecialUser) {
        return jpaUserRepository.countByIsSpecialUser(isSpecialUser);
    }
}
