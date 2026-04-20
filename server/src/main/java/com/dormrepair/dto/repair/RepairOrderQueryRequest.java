package com.dormrepair.dto.repair;

public record RepairOrderQueryRequest(
        String keyword,
        String status,
        Long repairTypeId,
        Long buildingId,
        Long repairerId,
        String dateFrom,
        String dateTo
) {
}
