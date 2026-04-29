package com.dormrepair.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * 找回密码第一步请求。
 * 用户先提交账号，系统返回该账号预先设置的安全问题；管理员账号不允许自助找回。
 */
public record ForgotPasswordQuestionRequest(
        @NotBlank String username
) {
}
