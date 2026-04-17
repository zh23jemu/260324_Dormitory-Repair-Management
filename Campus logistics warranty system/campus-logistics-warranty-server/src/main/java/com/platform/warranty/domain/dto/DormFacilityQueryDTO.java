package com.platform.warranty.domain.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DormFacilityQueryDTO extends PageQueryDTO {
    @Parameter(description = "房间ID")
    private String roomId;

    @Parameter(description = "物品名称")
    private String facilityName;

    @Parameter(description = "物品类型")
    private Byte facilityType;

    @Parameter(description = "品牌")
    private String brand;

    @Parameter(description = "状态")
    private Byte status;

    @Parameter(description = "关键词搜索（物品名称或品牌）")
    private String keyword;
}