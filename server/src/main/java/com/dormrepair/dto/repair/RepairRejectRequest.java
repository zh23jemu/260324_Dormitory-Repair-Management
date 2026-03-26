package com.dormrepair.dto.repair;

import jakarta.validation.constraints.NotBlank;

public record RepairRejectRequest(@NotBlank String rejectReason) {
}
