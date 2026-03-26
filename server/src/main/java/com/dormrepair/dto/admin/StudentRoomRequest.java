package com.dormrepair.dto.admin;

public record StudentRoomRequest(
        Long buildingId,
        Long roomId,
        String bedNo
) {
}
