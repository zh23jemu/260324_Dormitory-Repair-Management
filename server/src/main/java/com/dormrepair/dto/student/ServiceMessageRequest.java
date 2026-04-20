package com.dormrepair.dto.student;

import jakarta.validation.constraints.NotBlank;

public record ServiceMessageRequest(
        @NotBlank String title,
        @NotBlank String content,
        String imagePath
) {
}
