package com.devmentor.infrastructure.ai;

import com.devmentor.application.ai.AIReviewService;
import com.devmentor.domain.codereview.CodeQualityScore;
import com.devmentor.domain.codereview.FindingCategory;
import com.devmentor.domain.codereview.FindingSeverity;
import com.devmentor.domain.codereview.ReviewFinding;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * LangChain4j implementation of AI Review Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LangChain4jReviewService implements AIReviewService {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<ReviewFinding> analyzeFile(String filePath, String fileContent, String language) {
        log.info("Analyzing file: {} (language: {})", filePath, language);

        try {
            String prompt = PromptTemplates.fill(
                    PromptTemplates.FILE_REVIEW_TEMPLATE,
                    "filePath", filePath,
                    "language", language,
                    "code", fileContent
            );

            String response = chatModel.generate(prompt);
            log.debug("AI Response: {}", response);

            return parseFindings(response, filePath);

        } catch (Exception e) {
            log.error("Error analyzing file: {}", filePath, e);
            return createErrorFinding(filePath, e);
        }
    }

    @Override
    public List<ReviewFinding> analyzeProject(Map<String, String> files) {
        log.info("Analyzing project with {} files", files.size());

        List<ReviewFinding> allFindings = new ArrayList<>();

        // Analyze each file individually
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String language = detectLanguage(entry.getKey());
            List<ReviewFinding> fileFindings = analyzeFile(
                    entry.getKey(),
                    entry.getValue(),
                    language
            );
            allFindings.addAll(fileFindings);
        }

        // TODO: Add cross-file analysis for architecture issues

        return allFindings;
    }

    @Override
    public String generateSummary(List<ReviewFinding> findings) {
        log.info("Generating summary for {} findings", findings.size());

        try {
            String findingsJson = objectMapper.writeValueAsString(findings);

            String prompt = PromptTemplates.fill(
                    PromptTemplates.SUMMARY_TEMPLATE,
                    "findings", findingsJson
            );

            return chatModel.generate(prompt);

        } catch (Exception e) {
            log.error("Error generating summary", e);
            return "Error generating summary: " + e.getMessage();
        }
    }

    @Override
    public CodeQualityScore calculateQualityScore(List<ReviewFinding> findings) {
        log.info("Calculating quality score from {} findings", findings.size());

        // Count findings by severity and category
        int criticalCount = (int) findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.CRITICAL)
                .count();
        int highCount = (int) findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.HIGH)
                .count();
        int mediumCount = (int) findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.MEDIUM)
                .count();
        int lowCount = (int) findings.stream()
                .filter(f -> f.getSeverity() == FindingSeverity.LOW)
                .count();

        // Calculate individual scores
        int securityScore = calculateCategoryScore(findings, FindingCategory.SECURITY_VULNERABILITY);
        int performanceScore = calculateCategoryScore(findings, FindingCategory.PERFORMANCE);
        int maintainabilityScore = calculateMaintainabilityScore(findings);
        int bestPracticesScore = calculateCategoryScore(findings, FindingCategory.BEST_PRACTICE);
        int testCoverageScore = 70; // TODO: Calculate from actual test presence

        return CodeQualityScore.calculate(
                securityScore,
                performanceScore,
                maintainabilityScore,
                bestPracticesScore,
                testCoverageScore
        );
    }

    /**
     * Parse AI response into ReviewFinding objects
     */
    private List<ReviewFinding> parseFindings(String aiResponse, String filePath) {
        try {
            // Extract JSON from response (may be wrapped in markdown)
            String json = extractJson(aiResponse);

            // Parse JSON array
            List<Map<String, Object>> rawFindings = objectMapper.readValue(
                    json,
                    new TypeReference<>() {}
            );

            List<ReviewFinding> findings = new ArrayList<>();

            for (Map<String, Object> raw : rawFindings) {
                ReviewFinding finding = ReviewFinding.builder()
                        .title(getString(raw, "title"))
                        .description(getString(raw, "description"))
                        .severity(parseSeverity(getString(raw, "severity")))
                        .category(parseCategory(getString(raw, "category")))
                        .filePath(filePath)
                        .lineNumber(getInteger(raw, "lineNumber"))
                        .codeSnippet(getString(raw, "codeSnippet"))
                        .suggestedFix(getString(raw, "suggestedFix"))
                        .explanation(getString(raw, "explanation"))
                        .resourcesUrl(getString(raw, "resourcesUrl"))
                        .metricsViolated(getMetricsViolated(raw))
                        .impactScore(calculateImpact(raw))
                        .isResolved(false)
                        .build();

                findings.add(finding);
            }

            log.info("Parsed {} findings from AI response", findings.size());
            return findings;

        } catch (Exception e) {
            log.error("Error parsing findings from AI response", e);
            return createErrorFinding(filePath, e);
        }
    }

    /**
     * Extract JSON from AI response (handles markdown code blocks)
     */
    private String extractJson(String response) {
        // Remove markdown code blocks if present
        response = response.trim();

        if (response.startsWith("```json")) {
            response = response.substring(7);
        } else if (response.startsWith("```")) {
            response = response.substring(3);
        }

        if (response.endsWith("```")) {
            response = response.substring(0, response.length() - 3);
        }

        return response.trim();
    }

    /**
     * Calculate score for a specific category
     */
    private int calculateCategoryScore(List<ReviewFinding> findings, FindingCategory category) {
        int score = 100;

        long categoryFindings = findings.stream()
                .filter(f -> f.getCategory() == category)
                .count();

        for (ReviewFinding finding : findings) {
            if (finding.getCategory() == category) {
                score -= switch (finding.getSeverity()) {
                    case CRITICAL -> 20;
                    case HIGH -> 10;
                    case MEDIUM -> 5;
                    case LOW -> 2;
                    case INFO -> 0;
                };
            }
        }

        return Math.max(0, score);
    }

    /**
     * Calculate maintainability score
     */
    private int calculateMaintainabilityScore(List<ReviewFinding> findings) {
        int score = 100;

        for (ReviewFinding finding : findings) {
            if (finding.getCategory() == FindingCategory.CODE_SMELL ||
                finding.getCategory() == FindingCategory.NAMING ||
                finding.getCategory() == FindingCategory.DUPLICATION) {

                score -= switch (finding.getSeverity()) {
                    case CRITICAL -> 15;
                    case HIGH -> 8;
                    case MEDIUM -> 4;
                    case LOW -> 2;
                    case INFO -> 0;
                };
            }
        }

        return Math.max(0, score);
    }

    /**
     * Calculate impact score (1-10)
     */
    private int calculateImpact(Map<String, Object> raw) {
        FindingSeverity severity = parseSeverity(getString(raw, "severity"));
        return switch (severity) {
            case CRITICAL -> 10;
            case HIGH -> 7;
            case MEDIUM -> 5;
            case LOW -> 3;
            case INFO -> 1;
        };
    }

    /**
     * Detect programming language from file extension
     */
    private String detectLanguage(String filePath) {
        if (filePath.endsWith(".java")) return "java";
        if (filePath.endsWith(".kt")) return "kotlin";
        if (filePath.endsWith(".xml")) return "xml";
        if (filePath.endsWith(".yml") || filePath.endsWith(".yaml")) return "yaml";
        if (filePath.endsWith(".properties")) return "properties";
        return "text";
    }

    /**
     * Parse severity from string
     */
    private FindingSeverity parseSeverity(String severity) {
        try {
            return FindingSeverity.valueOf(severity.toUpperCase());
        } catch (Exception e) {
            return FindingSeverity.MEDIUM;
        }
    }

    /**
     * Parse category from string
     */
    private FindingCategory parseCategory(String category) {
        try {
            return FindingCategory.valueOf(category.toUpperCase());
        } catch (Exception e) {
            return FindingCategory.CODE_SMELL;
        }
    }

    /**
     * Create error finding when analysis fails
     */
    private List<ReviewFinding> createErrorFinding(String filePath, Exception e) {
        ReviewFinding errorFinding = ReviewFinding.builder()
                .title("Analysis Error")
                .description("Failed to analyze file: " + e.getMessage())
                .severity(FindingSeverity.INFO)
                .category(FindingCategory.CODE_SMELL)
                .filePath(filePath)
                .isResolved(false)
                .build();

        return List.of(errorFinding);
    }

    // Helper methods for parsing
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * Extract metricsViolated from raw finding map
     * Can be either a JSON array or comma-separated string
     */
    private String getMetricsViolated(Map<String, Object> map) {
        Object value = map.get("metricsViolated");
        if (value == null) {
            return null;
        }

        try {
            // If it's already a JSON array, serialize it
            if (value instanceof java.util.List) {
                return objectMapper.writeValueAsString(value);
            }
            // Otherwise, convert to string
            return value.toString();
        } catch (Exception e) {
            log.warn("Failed to parse metricsViolated: {}", e.getMessage());
            return null;
        }
    }
}
