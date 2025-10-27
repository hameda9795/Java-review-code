package com.devmentor.application.codereview;

import com.devmentor.domain.codereview.CodeReview;
import com.devmentor.domain.codereview.ReviewFinding;
import com.devmentor.domain.codereview.FindingSeverity;
import com.devmentor.domain.codereview.FindingCategory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating AI-ready prompts from code review findings
 * Creates comprehensive, structured prompts optimized for AI code fixing agents
 */
@Service
public class PromptGenerationService {

    /**
     * Generates a comprehensive prompt for AI to fix all issues in the code review
     */
    public String generateFixingPrompt(CodeReview review) {
        StringBuilder prompt = new StringBuilder();
        
        // Header with context
        prompt.append("# Code Review Issue Resolution Request\n\n");
        prompt.append("## Project Context\n");
        prompt.append(String.format("**Review ID:** %s\n", review.getId()));
        prompt.append(String.format("**Project:** %s\n", review.getTitle()));
        prompt.append(String.format("**Files Analyzed:** %d files (%d lines of code)\n", 
            review.getTotalFilesAnalyzed(), review.getTotalLinesAnalyzed()));
        prompt.append(String.format("**Overall Quality Score:** %d/100\n\n", 
            review.getQualityScore() != null ? review.getQualityScore().getOverallScore() : 0));
        
        // Quality breakdown
        if (review.getQualityScore() != null) {
            prompt.append("### Quality Metrics\n");
            prompt.append(String.format("- Security: %d/100\n", review.getQualityScore().getSecurityScore()));
            prompt.append(String.format("- Performance: %d/100\n", review.getQualityScore().getPerformanceScore()));
            prompt.append(String.format("- Maintainability: %d/100\n", review.getQualityScore().getMaintainabilityScore()));
            prompt.append(String.format("- Best Practices: %d/100\n", review.getQualityScore().getBestPracticesScore()));
            prompt.append(String.format("- Test Coverage: %d/100\n\n", review.getQualityScore().getTestCoverageScore()));
        }
        
        // Statistics
        var findings = review.getFindings();
        Map<FindingSeverity, Long> severityCounts = findings.stream()
            .collect(Collectors.groupingBy(ReviewFinding::getSeverity, Collectors.counting()));
        
        prompt.append("## Issues Summary\n");
        prompt.append(String.format("**Total Issues:** %d\n\n", findings.size()));
        prompt.append("**By Severity:**\n");
        prompt.append(String.format("- 🔴 CRITICAL: %d (Must fix immediately - security/data loss risks)\n", 
            severityCounts.getOrDefault(FindingSeverity.CRITICAL, 0L)));
        prompt.append(String.format("- 🟠 HIGH: %d (Should fix before production)\n", 
            severityCounts.getOrDefault(FindingSeverity.HIGH, 0L)));
        prompt.append(String.format("- 🟡 MEDIUM: %d (Important improvements)\n", 
            severityCounts.getOrDefault(FindingSeverity.MEDIUM, 0L)));
        prompt.append(String.format("- 🟢 LOW: %d (Nice to have)\n", 
            severityCounts.getOrDefault(FindingSeverity.LOW, 0L)));
        prompt.append(String.format("- ℹ️ INFO: %d (Suggestions)\n\n", 
            severityCounts.getOrDefault(FindingSeverity.INFO, 0L)));
        
        prompt.append("---\n\n");
        prompt.append("## YOUR TASK\n\n");
        prompt.append("You are an expert software engineer specializing in code quality and security. ");
        prompt.append("Your task is to **fix ALL the issues** listed below in the codebase. ");
        prompt.append("Follow these principles:\n\n");
        
        prompt.append("### Core Principles:\n");
        prompt.append("1. **Fix, Don't Just Comment** - Implement actual solutions, not TODO comments\n");
        prompt.append("2. **Maintain Functionality** - Ensure all existing features continue to work\n");
        prompt.append("3. **Follow Best Practices** - Apply industry-standard patterns and conventions\n");
        prompt.append("4. **Prioritize by Severity** - Fix CRITICAL and HIGH severity issues first\n");
        prompt.append("5. **Document Changes** - Add clear comments explaining complex fixes\n");
        prompt.append("6. **Test Your Changes** - Ensure fixes don't introduce new bugs\n");
        prompt.append("7. **Preserve Code Style** - Match existing formatting and naming conventions\n\n");
        
        prompt.append("### Implementation Guidelines:\n");
        prompt.append("- For **SECURITY** issues: Apply proper input validation, use parameterized queries, implement authentication/authorization\n");
        prompt.append("- For **PERFORMANCE** issues: Optimize algorithms, add caching, fix N+1 queries, add database indexes\n");
        prompt.append("- For **CODE_SMELL** issues: Refactor for clarity, reduce complexity, eliminate duplication\n");
        prompt.append("- For **BUG** issues: Fix logic errors, handle edge cases, add null checks\n");
        prompt.append("- For **ARCHITECTURE** issues: Properly separate concerns, follow SOLID principles, use appropriate design patterns\n");
        prompt.append("- For **TESTING** issues: Add unit tests, integration tests, improve test coverage\n\n");
        
        prompt.append("---\n\n");
        prompt.append("## DETAILED ISSUES TO FIX\n\n");
        
        // Add findings grouped by severity (most critical first)
        List<FindingSeverity> severityOrder = List.of(
            FindingSeverity.CRITICAL,
            FindingSeverity.HIGH,
            FindingSeverity.MEDIUM,
            FindingSeverity.LOW,
            FindingSeverity.INFO
        );
        
        int issueNumber = 1;
        for (FindingSeverity severity : severityOrder) {
            List<ReviewFinding> severityFindings = findings.stream()
                .filter(f -> f.getSeverity() == severity)
                .sorted((a, b) -> {
                    // Sort by category importance within severity
                    int categoryOrder1 = getCategoryImportance(a.getCategory());
                    int categoryOrder2 = getCategoryImportance(b.getCategory());
                    return Integer.compare(categoryOrder1, categoryOrder2);
                })
                .toList();
            
            if (severityFindings.isEmpty()) continue;
            
            String severityEmoji = getSeverityEmoji(severity);
            prompt.append(String.format("### %s %s Priority Issues\n\n", severityEmoji, severity.name()));
            
            for (ReviewFinding finding : severityFindings) {
                prompt.append(String.format("#### Issue #%d: %s\n\n", issueNumber++, finding.getTitle()));
                prompt.append(String.format("**Category:** %s\n", finding.getCategory()));
                prompt.append(String.format("**Severity:** %s\n", finding.getSeverity()));
                prompt.append(String.format("**File:** `%s` (Line %d)\n\n", finding.getFilePath(), finding.getLineNumber()));
                
                prompt.append("**Problem Description:**\n");
                prompt.append(finding.getDescription()).append("\n\n");
                
                if (finding.getCodeSnippet() != null && !finding.getCodeSnippet().isBlank()) {
                    prompt.append("**Current Code:**\n");
                    prompt.append("```java\n");
                    prompt.append(finding.getCodeSnippet()).append("\n");
                    prompt.append("```\n\n");
                }
                
                if (finding.getSuggestedFix() != null && !finding.getSuggestedFix().isBlank()) {
                    prompt.append("**Suggested Solution:**\n");
                    prompt.append(finding.getSuggestedFix()).append("\n\n");
                }
                
                if (finding.getImpactScore() != null) {
                    prompt.append("**Impact Level:**\n");
                    prompt.append(String.format("Impact Score: %d/10\n\n", finding.getImpactScore()));
                }
                
                prompt.append("**Action Required:**\n");
                prompt.append(generateActionableInstruction(finding)).append("\n\n");
                
                prompt.append("---\n\n");
            }
        }
        
        // Footer with final instructions
        prompt.append("## FINAL INSTRUCTIONS\n\n");
        prompt.append("### Deliverables:\n");
        prompt.append("1. **Complete Fixed Codebase** - All issues resolved with working code\n");
        prompt.append("2. **Change Summary** - Brief description of each fix applied\n");
        prompt.append("3. **Verification Steps** - How to verify fixes work correctly\n");
        prompt.append("4. **Testing Notes** - Any new tests added or existing tests updated\n\n");
        
        prompt.append("### Quality Checklist (Verify before submitting):\n");
        prompt.append("- [ ] All CRITICAL issues fixed\n");
        prompt.append("- [ ] All HIGH priority issues fixed\n");
        prompt.append("- [ ] Code compiles without errors\n");
        prompt.append("- [ ] No new bugs introduced\n");
        prompt.append("- [ ] Tests pass successfully\n");
        prompt.append("- [ ] Security vulnerabilities eliminated\n");
        prompt.append("- [ ] Performance optimizations applied\n");
        prompt.append("- [ ] Code follows project conventions\n");
        prompt.append("- [ ] Documentation updated where needed\n\n");
        
        prompt.append("### Success Criteria:\n");
        prompt.append("Your implementation will be successful when:\n");
        prompt.append("1. The code quality score increases significantly (target: 80+/100)\n");
        prompt.append("2. All security vulnerabilities are eliminated\n");
        prompt.append("3. The application runs without errors\n");
        prompt.append("4. Best practices are consistently applied\n");
        prompt.append("5. The code is maintainable and well-documented\n\n");
        
        prompt.append("**Remember:** You're not just fixing issues—you're improving code quality, ");
        prompt.append("security, and maintainability for long-term success. Every fix should make the ");
        prompt.append("codebase better, safer, and more professional.\n\n");
        
        prompt.append("---\n\n");
        prompt.append("**Good luck! Fix these issues and deliver production-ready code. 🚀**\n");
        
        return prompt.toString();
    }
    
    private String getSeverityEmoji(FindingSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "🔴";
            case HIGH -> "🟠";
            case MEDIUM -> "🟡";
            case LOW -> "🟢";
            case INFO -> "ℹ️";
        };
    }
    
    private int getCategoryImportance(FindingCategory category) {
        // Lower number = higher importance
        return switch (category) {
            case SECURITY_VULNERABILITY -> 1;
            case BUG -> 2;
            case PERFORMANCE -> 3;
            case ARCHITECTURE -> 4;
            case ERROR_HANDLING -> 5;
            case DATABASE -> 6;
            case TESTING -> 7;
            case CODE_SMELL -> 8;
            case BEST_PRACTICE -> 9;
            case API_DESIGN -> 10;
            case CONFIGURATION -> 11;
            case DEPENDENCY -> 12;
            case DOCUMENTATION -> 13;
            case NAMING -> 14;
            case DUPLICATION -> 15;
            case DESIGN_PATTERN -> 16;
        };
    }
    
    private String generateActionableInstruction(ReviewFinding finding) {
        StringBuilder instruction = new StringBuilder();
        
        switch (finding.getCategory()) {
            case SECURITY_VULNERABILITY -> {
                instruction.append("🔒 **SECURITY FIX REQUIRED:**\n");
                instruction.append("- Implement proper security controls immediately\n");
                instruction.append("- Validate all inputs and sanitize outputs\n");
                instruction.append("- Use parameterized queries for database operations\n");
                instruction.append("- Apply proper authentication and authorization checks\n");
            }
            case BUG -> {
                instruction.append("🐛 **BUG FIX REQUIRED:**\n");
                instruction.append("- Analyze the root cause of the issue\n");
                instruction.append("- Implement a robust fix that handles edge cases\n");
                instruction.append("- Add unit tests to prevent regression\n");
                instruction.append("- Verify the fix doesn't break existing functionality\n");
            }
            case PERFORMANCE -> {
                instruction.append("⚡ **PERFORMANCE OPTIMIZATION REQUIRED:**\n");
                instruction.append("- Profile the code to identify bottlenecks\n");
                instruction.append("- Implement efficient algorithms and data structures\n");
                instruction.append("- Add caching where appropriate\n");
                instruction.append("- Optimize database queries (add indexes, avoid N+1)\n");
            }
            case ARCHITECTURE -> {
                instruction.append("🏗️ **ARCHITECTURAL IMPROVEMENT REQUIRED:**\n");
                instruction.append("- Refactor to follow SOLID principles\n");
                instruction.append("- Properly separate concerns (Domain, Application, Infrastructure)\n");
                instruction.append("- Use appropriate design patterns\n");
                instruction.append("- Ensure loose coupling and high cohesion\n");
            }
            case CODE_SMELL -> {
                instruction.append("🧹 **CODE REFACTORING REQUIRED:**\n");
                instruction.append("- Simplify complex methods (reduce cyclomatic complexity)\n");
                instruction.append("- Extract reusable code into separate methods/classes\n");
                instruction.append("- Improve code readability and maintainability\n");
                instruction.append("- Remove code duplication\n");
            }
            case TESTING -> {
                instruction.append("🧪 **TESTING IMPROVEMENT REQUIRED:**\n");
                instruction.append("- Add comprehensive unit tests\n");
                instruction.append("- Implement integration tests for critical paths\n");
                instruction.append("- Increase code coverage to 80%+\n");
                instruction.append("- Test edge cases and error scenarios\n");
            }
            case ERROR_HANDLING -> {
                instruction.append("⚠️ **ERROR HANDLING REQUIRED:**\n");
                instruction.append("- Add proper try-catch blocks\n");
                instruction.append("- Implement custom exceptions for business logic\n");
                instruction.append("- Log errors with appropriate context\n");
                instruction.append("- Provide meaningful error messages to users\n");
            }
            case DATABASE -> {
                instruction.append("💾 **DATABASE OPTIMIZATION REQUIRED:**\n");
                instruction.append("- Add proper indexes for query performance\n");
                instruction.append("- Use transactions appropriately\n");
                instruction.append("- Implement connection pooling\n");
                instruction.append("- Optimize queries (avoid SELECT *, use projections)\n");
            }
            default -> {
                instruction.append("✅ **FIX REQUIRED:**\n");
                instruction.append("- Address the issue as described above\n");
                instruction.append("- Follow best practices for this type of problem\n");
                instruction.append("- Ensure the fix is maintainable and well-documented\n");
            }
        }
        
        return instruction.toString();
    }
}
