package com.dormrepair.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record PasswordRequest(
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {
}
