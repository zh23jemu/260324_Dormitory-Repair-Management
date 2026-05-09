package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 班级基础数据保存请求。
 * 班级必须挂在某个专业下，学生注册时只允许选择后台已维护的班级。
 */
public record ClassRequest(
        @NotNull Long majorId,
        @NotBlank String className,
        Integer sortNo,
        String status
) {
}
