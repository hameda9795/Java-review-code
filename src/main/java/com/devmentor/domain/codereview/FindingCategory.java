package com.devmentor.domain.codereview;

/**
 * Categories of code review findings
 */
public enum FindingCategory {
    SECURITY_VULNERABILITY("Security issues that could be exploited"),
    PERFORMANCE("Performance bottlenecks and inefficiencies"),
    CODE_SMELL("Code that works but is hard to maintain"),
    BUG("Actual or potential bugs"),
    ARCHITECTURE("Architectural and layer violations"),
    DESIGN_PATTERN("Design pattern opportunities"),
    BEST_PRACTICE("Spring Boot and Java best practices"),
    TESTING("Testing related improvements"),
    DOCUMENTATION("Missing or inadequate documentation"),
    ERROR_HANDLING("Exception and error handling issues"),
    DEPENDENCY("Dependency management issues"),
    CONFIGURATION("Configuration and property issues"),
    DATABASE("Database and JPA related issues"),
    API_DESIGN("REST API design improvements"),
    NAMING("Naming conventions and clarity"),
    DUPLICATION("Code duplication");

    private final String description;

    FindingCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
