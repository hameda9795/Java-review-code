package com.devmentor.interfaces.rest.dto;

import java.time.LocalDateTime;

/**
 * DTO for generated AI fixing prompt
 */
public record GeneratedPromptDTO(
    String prompt,
    int totalFindings,
    int criticalCount,
    int highCount,
    int mediumCount,
    int lowCount,
    String reviewId,
    LocalDateTime generatedAt,
    String instructions
) {}
