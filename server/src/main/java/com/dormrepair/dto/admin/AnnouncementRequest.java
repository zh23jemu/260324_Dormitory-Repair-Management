package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AnnouncementRequest(
        @NotBlank String title,
        @NotBlank String content,
        String imagePath,
        String status
) {
}
