package com.devmentor.domain.project;

import com.devmentor.domain.shared.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * SourceFile entity representing a single source code file in a project
 */
@Entity
@Table(name = "source_files", indexes = {
        @Index(name = "idx_source_file_project_id", columnList = "project_id"),
        @Index(name = "idx_source_file_path", columnList = "file_path")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceFile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotBlank(message = "File path is required")
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @NotBlank(message = "File name is required")
    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "lines_of_code", nullable = false)
    @Builder.Default
    private Integer linesOfCode = 0;

    @Column(name = "file_size", nullable = false)
    @Builder.Default
    private Long fileSize = 0L;

    @Column(name = "language", length = 50)
    private String language;

    // Business methods
    public void updateContent(String content) {
        this.content = content;
        this.linesOfCode = countLines(content);
        this.fileSize = (long) content.length();
    }

    private int countLines(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return content.split("\r\n|\r|\n").length;
    }

    public boolean isJavaFile() {
        return "java".equalsIgnoreCase(fileExtension);
    }

    public boolean isTestFile() {
        return fileName.toLowerCase().contains("test") ||
               filePath.toLowerCase().contains("test");
    }
}
