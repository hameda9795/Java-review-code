package com.devmentor.domain.codereview;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Educational resource for learning from code review findings
 * Provides context, best practices, and references
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationalResource {
    
    /**
     * Learning objective - what the developer should understand after reading this
     */
    private String learningObjective;
    
    /**
     * Difficulty level of this concept
     */
    private DifficultyLevel difficultyLevel;
    
    /**
     * Design patterns related to this finding
     */
    private String relatedPatterns;
    
    /**
     * SOLID principle involved (if applicable)
     */
    private String solidPrinciple;
    
    /**
     * Theoretical foundation (why this matters)
     */
    private String theoreticalFoundation;
    
    /**
     * Real-world impact explanation
     */
    private String realWorldImpact;
    
    /**
     * Common misconceptions developers have
     */
    private String commonMisconceptions;
    
    /**
     * When NOT to apply this rule
     */
    private String exceptions;
    
    /**
     * External resources for deeper learning
     */
    private ResourceLinks resourceLinks;
    
    /**
     * Code examples showing before/after
     */
    private CodeExamples codeExamples;
    
    public enum DifficultyLevel {
        BEGINNER("Basic Java/Spring Boot knowledge"),
        INTERMEDIATE("Understanding of design patterns and SOLID principles"),
        ADVANCED("Deep architecture knowledge, enterprise patterns"),
        EXPERT("Advanced theoretical knowledge, performance tuning, security");
        
        private final String description;
        
        DifficultyLevel(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceLinks {
        private String officialDocumentation;
        private String bookReference; // e.g., "Effective Java Item 17"
        private String blogPost;
        private String videoTutorial;
        private String stackOverflowDiscussion;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeExamples {
        private String beforeCode;
        private String afterCode;
        private String explanation;
        private String tradeoffs;
    }
}
