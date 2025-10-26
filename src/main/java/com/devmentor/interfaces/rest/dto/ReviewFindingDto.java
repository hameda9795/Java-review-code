package com.devmentor.interfaces.rest.dto;

import com.devmentor.domain.codereview.FindingCategory;
import com.devmentor.domain.codereview.FindingSeverity;
import com.devmentor.domain.codereview.ReviewFinding;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * DTO for review finding
 */
@Data
@Builder
public class ReviewFindingDto {

    private UUID id;
    private String title;
    private String description;
    private FindingSeverity severity;
    private FindingCategory category;
    private String filePath;
    private Integer lineNumber;
    private Integer endLineNumber;
    private String codeSnippet;
    private String suggestedFix;
    private String explanation;
    private String resourcesUrl;
    private Integer impactScore;
    private Boolean isResolved;

    public static ReviewFindingDto from(ReviewFinding finding) {
        return ReviewFindingDto.builder()
                .id(finding.getId())
                .title(finding.getTitle())
                .description(finding.getDescription())
                .severity(finding.getSeverity())
                .category(finding.getCategory())
                .filePath(finding.getFilePath())
                .lineNumber(finding.getLineNumber())
                .endLineNumber(finding.getEndLineNumber())
                .codeSnippet(finding.getCodeSnippet())
                .suggestedFix(finding.getSuggestedFix())
                .explanation(finding.getExplanation())
                .resourcesUrl(finding.getResourcesUrl())
                .impactScore(finding.getImpactScore())
                .isResolved(finding.getIsResolved())
                .build();
    }
}
