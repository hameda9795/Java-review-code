package com.devmentor.infrastructure.github;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GitHub repository tree structure
 */
@Data
@Builder
public class GitHubTree {
    private String sha;
    private List<GitHubTreeItem> tree;

    @Data
    @Builder
    public static class GitHubTreeItem {
        private String path;
        private String mode;
        private String type;
        private String sha;
        private Long size;
        private String url;
    }

    @SuppressWarnings("unchecked")
    public static GitHubTree fromMap(Map<String, Object> map) {
        List<GitHubTreeItem> items = new ArrayList<>();

        if (map.containsKey("tree") && map.get("tree") instanceof List) {
            List<Map<String, Object>> treeList = (List<Map<String, Object>>) map.get("tree");
            for (Map<String, Object> item : treeList) {
                items.add(GitHubTreeItem.builder()
                        .path((String) item.get("path"))
                        .mode((String) item.get("mode"))
                        .type((String) item.get("type"))
                        .sha((String) item.get("sha"))
                        .size(item.get("size") != null ? ((Number) item.get("size")).longValue() : null)
                        .url((String) item.get("url"))
                        .build());
            }
        }

        return GitHubTree.builder()
                .sha((String) map.get("sha"))
                .tree(items)
                .build();
    }

    /**
     * Get only Java files from the tree
     */
    public List<GitHubTreeItem> getJavaFiles() {
        return tree.stream()
                .filter(item -> "blob".equals(item.getType()))
                .filter(item -> item.getPath().endsWith(".java"))
                .toList();
    }

    /**
     * Get files matching a pattern
     */
    public List<GitHubTreeItem> getFilesByPattern(String pattern) {
        return tree.stream()
                .filter(item -> "blob".equals(item.getType()))
                .filter(item -> item.getPath().matches(pattern))
                .toList();
    }
}
