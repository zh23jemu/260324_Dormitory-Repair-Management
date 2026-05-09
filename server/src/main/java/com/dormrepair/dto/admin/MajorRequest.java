package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 专业基础数据保存请求。
 * 每个专业必须归属一个已存在的学院，用于保证“学院 -> 专业 -> 班级”的层级完整性。
 */
public record MajorRequest(
        @NotNull Long collegeId,
        @NotBlank String majorName,
        Integer sortNo,
        String status
) {
}
