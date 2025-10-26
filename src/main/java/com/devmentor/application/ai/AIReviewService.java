package com.devmentor.application.ai;

import com.devmentor.domain.codereview.ReviewFinding;

import java.util.List;

/**
 * Port interface for AI Review Service
 * This defines what the application layer needs from AI infrastructure
 */
public interface AIReviewService {

    /**
     * Analyze a single file and return findings
     *
     * @param filePath Path to the file
     * @param fileContent Content of the file
     * @param language Programming language
     * @return List of review findings
     */
    List<ReviewFinding> analyzeFile(String filePath, String fileContent, String language);

    /**
     * Analyze multiple files together for contextual review
     *
     * @param files Map of file paths to content
     * @return List of review findings across all files
     */
    List<ReviewFinding> analyzeProject(java.util.Map<String, String> files);

    /**
     * Generate a summary and recommendations for the entire review
     *
     * @param findings All findings from the review
     * @return Summary text
     */
    String generateSummary(List<ReviewFinding> findings);

    /**
     * Calculate quality scores based on findings
     *
     * @param findings All findings from the review
     * @return Quality scores object
     */
    com.devmentor.domain.codereview.CodeQualityScore calculateQualityScore(List<ReviewFinding> findings);
}
