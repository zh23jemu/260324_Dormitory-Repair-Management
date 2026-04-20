package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FacilityRequest(
        @NotNull Long roomId,
        @NotBlank String facilityName,
        @NotBlank String facilityType,
        String brand,
        String modelNumber,
        String purchaseDate,
        String status,
        String remark
) {
}
