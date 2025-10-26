package com.devmentor.domain.codereview;

/**
 * Severity levels for review findings
 */
public enum FindingSeverity {
    CRITICAL(4, "Must fix immediately"),
    HIGH(3, "Should fix soon"),
    MEDIUM(2, "Should fix eventually"),
    LOW(1, "Nice to have"),
    INFO(0, "Informational only");

    private final int priority;
    private final String description;

    FindingSeverity(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }
}
