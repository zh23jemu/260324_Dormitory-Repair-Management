package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DictSaveRequest(
        @NotBlank String dictType,
        @NotBlank String dictCode,
        @NotBlank String dictName,
        @NotNull Integer sortNo,
        @NotBlank String status
) {
}
