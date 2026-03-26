package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRequest(
        @NotNull Long buildingId,
        @NotBlank String roomNo,
        @NotNull Integer capacity,
        String facilityDesc,
        String status,
        String remark
) {
}
