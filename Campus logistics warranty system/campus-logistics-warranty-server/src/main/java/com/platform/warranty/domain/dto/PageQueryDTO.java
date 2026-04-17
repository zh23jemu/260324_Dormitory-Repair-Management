package com.platform.warranty.domain.dto;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class PageQueryDTO {
    @Parameter(description = "页码", required = true)
    private Integer pageNum = 1; // 默认值为 1

    @Parameter(description = "每页大小", required = true)
    private Integer pageSize = 10; // 默认值为 10
}
