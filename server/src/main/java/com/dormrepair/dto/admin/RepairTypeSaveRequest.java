package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RepairTypeSaveRequest(
        @NotBlank String typeName,
        @NotNull Integer sortNo,
        @NotBlank String status
) {
}
