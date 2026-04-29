package com.dormrepair.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录后维护安全问题的请求。
 * 安全答案不会在查询接口中回显，更新时必须同时提交新问题和新答案。
 */
public record SecurityQuestionRequest(
        @NotBlank String question,
        @NotBlank String answer
) {
}
