package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String realName,
        String phone,
        @NotBlank String role,
        String workTypeCode,
        String passwordQuestion,
        String passwordAnswer
) {
}
