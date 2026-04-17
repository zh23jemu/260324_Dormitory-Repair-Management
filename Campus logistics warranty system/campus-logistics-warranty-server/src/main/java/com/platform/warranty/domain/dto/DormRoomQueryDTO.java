package com.platform.warranty.domain.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DormRoomQueryDTO extends PageQueryDTO {
    @Parameter(description = "房间ID")
    private String roomId;

    @Parameter(description = "所属楼栋编号")
    private String buildingId;

    @Parameter(description = "楼层")
    private Byte floor;

    @Parameter(description = "房间号")
    private String roomNumber;

    @Parameter(description = "房型")
    private Byte roomType;

    @Parameter(description = "状态")
    private Byte status;

    @Parameter(description = "关键词搜索（房间ID或房间号）")
    private String keyword;
}