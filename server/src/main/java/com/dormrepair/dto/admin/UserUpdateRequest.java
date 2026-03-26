package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateRequest(
        @NotBlank String realName,
        String phone,
        @NotBlank String role
) {
}
