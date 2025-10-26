package com.devmentor.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * DTO for creating a new code review from uploaded files
 */
@Data
public class CreateReviewRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Files are required")
    @Size(min = 1, max = 100, message = "Must provide between 1 and 100 files")
    private Map<String, String> files; // filePath -> content
}
