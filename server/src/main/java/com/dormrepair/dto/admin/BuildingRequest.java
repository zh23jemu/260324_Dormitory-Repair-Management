package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BuildingRequest(
        @NotBlank String buildingName,
        @NotBlank String buildingCode,
        String genderType,
        @NotNull Integer floorCount,
        String remark
) {
}
