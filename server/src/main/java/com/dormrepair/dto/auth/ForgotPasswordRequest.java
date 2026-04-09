package com.dormrepair.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank String username,
        @NotBlank String studentNo,
        @NotBlank String phone,
        @NotBlank String newPassword
) {
}
