package com.devmentor.infrastructure.github;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * GitHub API client for OAuth and repository operations
 */
@Slf4j
@Component
public class GitHubClient {

    private final RestTemplate restTemplate;
    private final String clientId;
    private final String clientSecret;

    public GitHubClient(
            RestTemplate restTemplate,
            @Value("${github.oauth.client-id}") String clientId,
            @Value("${github.oauth.client-secret}") String clientSecret
    ) {
        this.restTemplate = restTemplate;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        
        log.info("=== GitHub Client Initialized ===");
        log.info("Client ID: {}", clientId != null && !clientId.isEmpty() ? clientId.substring(0, Math.min(10, clientId.length())) + "..." : "NOT SET");
        log.info("Client Secret: {}", clientSecret != null && !clientSecret.isEmpty() ? "SET (length: " + clientSecret.length() + ")" : "NOT SET");
    }

    /**
     * Exchange OAuth code for access token
     */
    public String getAccessToken(String code) {
        String url = "https://github.com/login/oauth/access_token";

        log.info("=== Requesting GitHub access token ===");
        log.info("URL: {}", url);
        log.info("Code: {}", code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "NULL");
        log.info("Client ID: {}", clientId != null && !clientId.isEmpty() ? clientId.substring(0, Math.min(10, clientId.length())) + "..." : "NOT SET");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        Map<String, String> requestBody = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Sending request to GitHub...");
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            log.info("GitHub response status: {}", response.getStatusCode());
            log.info("GitHub response body: {}", response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                
                // Check for error in response
                if (body.containsKey("error")) {
                    log.error("GitHub OAuth error: {}", body.get("error"));
                    log.error("Error description: {}", body.get("error_description"));
                    log.error("Error URI: {}", body.get("error_uri"));
                    return null;
                }
                
                String accessToken = (String) body.get("access_token");
                if (accessToken != null) {
                    log.info("Successfully obtained access token");
                    return accessToken;
                } else {
                    log.error("No access_token in response body: {}", body);
                    return null;
                }
            } else {
                log.error("Unexpected response status: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Exception while getting GitHub access token", e);
            log.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
        }

        return null;
    }

    /**
     * Get authenticated user information from GitHub
     */
    public GitHubUser getUser(String accessToken) {
        String url = "https://api.github.com/user";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return GitHubUser.builder()
                        .id(((Number) body.get("id")).longValue())
                        .login((String) body.get("login"))
                        .name((String) body.get("name"))
                        .email((String) body.get("email"))
                        .avatarUrl((String) body.get("avatar_url"))
                        .profileUrl((String) body.get("html_url"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to get GitHub user", e);
        }

        return null;
    }

    /**
     * Get repository information
     */
    public GitHubRepository getRepository(String owner, String repo, String accessToken) {
        String url = String.format("https://api.github.com/repos/%s/%s", owner, repo);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                return GitHubRepository.builder()
                        .id(((Number) body.get("id")).longValue())
                        .name((String) body.get("name"))
                        .fullName((String) body.get("full_name"))
                        .description((String) body.get("description"))
                        .url((String) body.get("html_url"))
                        .language((String) body.get("language"))
                        .defaultBranch((String) body.get("default_branch"))
                        .isPrivate((Boolean) body.get("private"))
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to get GitHub repository", e);
        }

        return null;
    }

    /**
     * Get file content from repository
     */
    public String getFileContent(String owner, String repo, String path, String accessToken) {
        String url = String.format("https://api.github.com/repos/%s/%s/contents/%s", owner, repo, path);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3.raw");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to get file content from GitHub", e);
        }

        return null;
    }

    /**
     * List user's repositories
     */
    public java.util.List<GitHubRepository> getUserRepositories(String accessToken) {
        String url = "https://api.github.com/user/repos?sort=updated&per_page=100";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<java.util.List> response = restTemplate.exchange(url, HttpMethod.GET, request, java.util.List.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                java.util.List<GitHubRepository> repositories = new java.util.ArrayList<>();
                
                for (Object item : response.getBody()) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> repoData = (Map<String, Object>) item;
                        
                        GitHubRepository repo = GitHubRepository.builder()
                                .id(((Number) repoData.get("id")).longValue())
                                .name((String) repoData.get("name"))
                                .fullName((String) repoData.get("full_name"))
                                .description((String) repoData.get("description"))
                                .url((String) repoData.get("html_url"))
                                .language((String) repoData.get("language"))
                                .defaultBranch((String) repoData.get("default_branch"))
                                .isPrivate((Boolean) repoData.get("private"))
                                .build();
                        
                        repositories.add(repo);
                    }
                }
                
                log.info("Retrieved {} repositories for user", repositories.size());
                return repositories;
            }
        } catch (Exception e) {
            log.error("Failed to get user repositories", e);
        }

        return new java.util.ArrayList<>();
    }

    /**
     * List repository files (recursive)
     */
    public GitHubTree getRepositoryTree(String owner, String repo, String sha, String accessToken) {
        String url = String.format("https://api.github.com/repos/%s/%s/git/trees/%s?recursive=1", owner, repo, sha);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return GitHubTree.fromMap(response.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to get repository tree", e);
        }

        return null;
    }
}
