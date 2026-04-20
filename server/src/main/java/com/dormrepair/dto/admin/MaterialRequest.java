package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 管理员维护维修耗材库存的请求。
 */
public record MaterialRequest(
        @NotBlank String materialName,
        String materialType,
        @NotBlank String unit,
        @NotNull Double stockQty,
        @NotNull Double warningQty,
        String remark
) {
}
