package com.dormrepair.dto.student;

public record StudentProfileUpdateRequest(
        String phone,
        Long buildingId,
        Long roomId,
        String bedNo
) {
}
