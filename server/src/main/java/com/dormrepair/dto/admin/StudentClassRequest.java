package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;

/**
 * 学生班级调整请求。
 * 学生档案中保存的是学院、专业、班级名称，提交前后端会校验三者必须来自已启用的层级数据。
 */
public record StudentClassRequest(
        @NotBlank String college,
        @NotBlank String major,
        @NotBlank String className
) {
}
