package com.devmentor.application.auth;

import com.devmentor.domain.user.SubscriptionTier;
import com.devmentor.domain.user.User;
import com.devmentor.domain.user.UserRepository;
import com.devmentor.domain.user.UserRole;
import com.devmentor.infrastructure.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service for user registration and login
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User register(String username, String email, String password, String fullName) {
        log.info("Registering new user: {}", username);

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(UserRole.USER)
                .subscriptionTier(SubscriptionTier.FREE)
                .isActive(true)
                .emailVerified(false)
                .reviewsCount(0)
                .totalFilesReviewed(0)
                .build();

        user = userRepository.save(user);

        log.info("User registered successfully: {}", username);
        return user;
    }

    public String login(String username, String password) {
        log.info("User login attempt: {}", username);

        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // Load user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Record login
        user.recordLogin();
        userRepository.save(user);

        // Generate token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail());

        log.info("User logged in successfully: {}", username);
        return token;
    }

    public User getUserFromToken(String token) {
        String username = jwtUtil.extractUsername(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
