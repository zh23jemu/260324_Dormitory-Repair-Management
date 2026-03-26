package com.dormrepair.dto.repair;

import jakarta.validation.constraints.NotNull;

public record RepairAssignRequest(
        @NotNull Long repairerId,
        String remark
) {
}
