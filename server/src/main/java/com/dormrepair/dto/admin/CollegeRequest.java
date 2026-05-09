package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;

/**
 * 学院基础数据保存请求。
 * 学院是专业和班级的上级节点，管理员需要先维护学院，后续才能在该学院下创建专业。
 */
public record CollegeRequest(
        @NotBlank String collegeName,
        Integer sortNo,
        String status
) {
}
