package com.dormrepair.dto.repair;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RepairRatingRequest(
        @NotNull @Min(1) @Max(5) Integer score,
        String content
) {
}
