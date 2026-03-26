package com.dormrepair.dto.repair;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record RepairFeedbackRequest(
        @NotBlank String resultDesc,
        String materialsUsed,
        @NotBlank String finishTime,
        List<String> imagePaths
) {
}
