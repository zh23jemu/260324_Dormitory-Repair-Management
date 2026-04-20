package com.dormrepair.dto.repair;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RepairCreateRequest(
        @NotNull Long repairTypeId,
        @NotBlank String title,
        @NotBlank String description,
        String expectTime,
        Long buildingId,
        Long roomId,
        Long facilityId,
        List<String> imagePaths
) {
}
