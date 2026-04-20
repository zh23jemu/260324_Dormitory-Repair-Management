package com.dormrepair.dto.student;

public record StudentProfileUpdateRequest(
        String phone,
        String avatar,
        Long buildingId,
        Long roomId,
        String bedNo
) {
}
