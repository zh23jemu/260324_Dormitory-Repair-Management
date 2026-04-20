package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResourceRequest(
        @NotBlank String title,
        @NotBlank String category,
        String summary,
        @NotBlank String content,
        String coverImage,
        @NotNull Integer sortNo,
        String status
) {
}
