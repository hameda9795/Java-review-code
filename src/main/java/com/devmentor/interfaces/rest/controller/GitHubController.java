package com.devmentor.interfaces.rest.controller;

import com.devmentor.application.service.GitHubService;
import com.devmentor.domain.user.User;
import com.devmentor.infrastructure.github.GitHubRepository;
import com.devmentor.infrastructure.security.JwtUtil;
import com.devmentor.interfaces.rest.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for GitHub OAuth and repository operations
 */
@Slf4j
@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubController {

    private final GitHubService gitHubService;
    private final JwtUtil jwtUtil;

    /**
     * Test endpoint to verify GitHub controller is accessible
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        log.info("GitHub test endpoint called");
        return ResponseEntity.ok(Map.of("status", "ok", "message", "GitHub controller is working"));
    }

    /**
     * GitHub OAuth callback - exchange code for access token
     */
    @PostMapping("/oauth/callback")
    public ResponseEntity<AuthResponse> handleOAuthCallback(@RequestParam String code) {
        log.info("=== GitHub OAuth callback received ===");
        log.info("Code parameter: {}", code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "null");

        try {
            User user = gitHubService.authenticateWithGitHub(code);
            log.info("User authenticated: {}", user.getUsername());

            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getEmail());

            AuthResponse response = new AuthResponse(
                    token,
                    "Bearer",
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getSubscriptionTier().name(),
                    user.getGithubUsername(),
                    user.hasGithubConnected()
            );

            log.info("Returning successful auth response");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during GitHub OAuth callback", e);
            throw e;
        }
    }

    /**
     * Get repository information
     */
    @GetMapping("/repos/{owner}/{repo}")
    public ResponseEntity<GitHubRepository> getRepository(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        GitHubRepository repository = gitHubService.getRepository(owner, repo, userId);
        if (repository == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repository);
    }

    /**
     * List user's repositories
     */
    @GetMapping("/repositories")
    public ResponseEntity<java.util.List<GitHubRepository>> getUserRepositories(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        java.util.List<GitHubRepository> repositories = gitHubService.getUserRepositories(userId);
        return ResponseEntity.ok(repositories);
    }

    /**
     * Analyze repository and get Java files
     */
    @PostMapping("/repos/{owner}/{repo}/analyze")
    public ResponseEntity<Map<String, String>> analyzeRepository(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        Map<String, String> files = gitHubService.analyzeRepository(owner, repo, userId);
        return ResponseEntity.ok(files);
    }

    /**
     * Get specific files from repository
     */
    @PostMapping("/repos/{owner}/{repo}/files")
    public ResponseEntity<Map<String, String>> getFiles(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        @SuppressWarnings("unchecked")
        java.util.List<String> filePaths = (java.util.List<String>) request.get("files");

        Map<String, String> files = gitHubService.getRepositoryFiles(owner, repo, filePaths, userId);
        return ResponseEntity.ok(files);
    }

    /**
     * Disconnect GitHub account
     */
    @DeleteMapping("/disconnect")
    public ResponseEntity<Void> disconnectGitHub(@RequestHeader("X-User-Id") UUID userId) {
        gitHubService.disconnectGitHub(userId);
        return ResponseEntity.noContent().build();
    }
}
