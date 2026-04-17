package com.platform.warranty.domain.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DormBuildingQueryDTO extends PageQueryDTO {
    @Parameter(description = "楼栋编号")
    private String buildingId;

    @Parameter(description = "楼栋名称")
    private String buildingName;

    @Parameter(description = "楼栋类型")
    private Byte buildingType;

    @Parameter(description = "状态")
    private Byte status;

    @Parameter(description = "关键词搜索（楼栋编号或名称）")
    private String keyword;
}