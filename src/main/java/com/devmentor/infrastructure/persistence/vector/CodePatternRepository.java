package com.devmentor.infrastructure.persistence.vector;

import com.devmentor.domain.codereview.CodePattern;
import com.devmentor.domain.codereview.FindingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for CodePattern with pgvector support
 */
@Repository
public interface CodePatternRepository extends JpaRepository<CodePattern, UUID> {

    List<CodePattern> findByCategory(FindingCategory category);

    List<CodePattern> findByPatternType(CodePattern.PatternType patternType);

    /**
     * Find similar patterns using vector similarity search
     */
    @Query(value = "SELECT * FROM code_patterns " +
            "ORDER BY embedding <-> CAST(:embedding AS vector) " +
            "LIMIT :limit", nativeQuery = true)
    List<CodePattern> findSimilarPatterns(
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );

    /**
     * Find similar patterns by category
     */
    @Query(value = "SELECT * FROM code_patterns " +
            "WHERE category = :category " +
            "ORDER BY embedding <-> CAST(:embedding AS vector) " +
            "LIMIT :limit", nativeQuery = true)
    List<CodePattern> findSimilarPatternsByCategory(
            @Param("embedding") String embedding,
            @Param("category") String category,
            @Param("limit") int limit
    );
}
