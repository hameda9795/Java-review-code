package com.devmentor.application.service;

import com.devmentor.domain.user.User;
import com.devmentor.domain.user.UserRepository;
import com.devmentor.infrastructure.github.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for GitHub integration and repository analysis
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubService {

    private final GitHubClient gitHubClient;
    private final UserRepository userRepository;

    /**
     * Authenticate user with GitHub OAuth (for new users logging in via GitHub)
     */
    @Transactional
    public User authenticateWithGitHub(String code) {
        // Exchange code for access token
        String accessToken = gitHubClient.getAccessToken(code);
        if (accessToken == null) {
            throw new RuntimeException("Failed to get GitHub access token");
        }

        // Get GitHub user information
        GitHubUser githubUser = gitHubClient.getUser(accessToken);
        if (githubUser == null) {
            throw new RuntimeException("Failed to get GitHub user information");
        }

        // Find or create user
        Optional<User> existingUser = userRepository.findByGithubId(githubUser.getId());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.updateGithubInfo(githubUser.getLogin(), accessToken);
            return userRepository.save(user);
        }

        // Create new user from GitHub
        User newUser = User.builder()
                .username(githubUser.getLogin())
                .email(githubUser.getEmail() != null ? githubUser.getEmail() : githubUser.getLogin() + "@github.com")
                .fullName(githubUser.getName() != null ? githubUser.getName() : githubUser.getLogin())
                .githubId(githubUser.getId())
                .githubUsername(githubUser.getLogin())
                .githubAccessToken(accessToken)
                .isActive(true)
                .build();

        return userRepository.save(newUser);
    }

    /**
     * Link GitHub account to an existing logged-in user
     */
    @Transactional
    public User linkGitHubAccount(String code, UUID userId) {
        // Exchange code for access token
        String accessToken = gitHubClient.getAccessToken(code);
        if (accessToken == null) {
            throw new RuntimeException("Failed to get GitHub access token");
        }

        // Get GitHub user information
        GitHubUser githubUser = gitHubClient.getUser(accessToken);
        if (githubUser == null) {
            throw new RuntimeException("Failed to get GitHub user information");
        }

        // Get the current logged-in user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if this GitHub account is already linked to another user
        Optional<User> existingGithubUser = userRepository.findByGithubId(githubUser.getId());
        if (existingGithubUser.isPresent() && !existingGithubUser.get().getId().equals(userId)) {
            throw new RuntimeException("This GitHub account is already linked to another user");
        }

        // Link GitHub account to the current user
        user.connectGithub(githubUser.getId(), githubUser.getLogin(), accessToken);
        return userRepository.save(user);
    }

    /**
     * Get repository information
     */
    public GitHubRepository getRepository(String owner, String repo, UUID userId) {
        User user = getUserWithGitHub(userId);
        return gitHubClient.getRepository(owner, repo, user.getGithubAccessToken());
    }

    /**
     * Analyze repository and extract Java files
     */
    public Map<String, String> analyzeRepository(String owner, String repo, UUID userId) {
        User user = getUserWithGitHub(userId);
        String accessToken = user.getGithubAccessToken();

        // Get repository info to get default branch
        GitHubRepository repository = gitHubClient.getRepository(owner, repo, accessToken);
        if (repository == null) {
            throw new RuntimeException("Repository not found or access denied");
        }

        // Get repository tree
        GitHubTree tree = gitHubClient.getRepositoryTree(owner, repo, repository.getDefaultBranch(), accessToken);
        if (tree == null) {
            throw new RuntimeException("Failed to get repository tree");
        }

        // Get all Java files
        List<GitHubTree.GitHubTreeItem> javaFiles = tree.getJavaFiles();
        log.info("Found {} Java files in {}/{}", javaFiles.size(), owner, repo);

        // Limit to prevent excessive API calls
        int maxFiles = 20;
        if (javaFiles.size() > maxFiles) {
            log.warn("Repository has {} Java files, limiting to {}", javaFiles.size(), maxFiles);
            javaFiles = javaFiles.stream().limit(maxFiles).toList();
        }

        // Fetch file contents
        Map<String, String> files = new LinkedHashMap<>();
        for (GitHubTree.GitHubTreeItem file : javaFiles) {
            String content = gitHubClient.getFileContent(owner, repo, file.getPath(), accessToken);
            if (content != null) {
                files.put(file.getPath(), content);
                log.debug("Fetched file: {}", file.getPath());
            }
        }

        return files;
    }

    /**
     * Get specific files from repository
     */
    public Map<String, String> getRepositoryFiles(String owner, String repo, List<String> filePaths, UUID userId) {
        User user = getUserWithGitHub(userId);
        String accessToken = user.getGithubAccessToken();

        Map<String, String> files = new LinkedHashMap<>();
        for (String path : filePaths) {
            String content = gitHubClient.getFileContent(owner, repo, path, accessToken);
            if (content != null) {
                files.put(path, content);
            }
        }

        return files;
    }

    /**
     * List user's GitHub repositories
     */
    public List<GitHubRepository> getUserRepositories(UUID userId) {
        User user = getUserWithGitHub(userId);
        return gitHubClient.getUserRepositories(user.getGithubAccessToken());
    }

    /**
     * Helper: Get user and validate GitHub connection
     */
    private User getUserWithGitHub(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.hasGithubConnected()) {
            throw new RuntimeException("GitHub account not connected");
        }

        return user;
    }

    /**
     * Disconnect GitHub account
     */
    @Transactional
    public void disconnectGitHub(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.disconnectGithub();
        userRepository.save(user);
    }
}
