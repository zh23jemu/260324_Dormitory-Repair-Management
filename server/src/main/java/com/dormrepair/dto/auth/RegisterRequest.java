package com.dormrepair.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String realName,
        @NotBlank String phone,
        @NotBlank String studentNo,
        @NotBlank String college,
        @NotBlank String major,
        @NotBlank String className
) {
}
