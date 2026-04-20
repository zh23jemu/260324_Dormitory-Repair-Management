package com.dormrepair.dto.student;

import jakarta.validation.constraints.NotBlank;

/**
 * 学生论坛发帖请求。
 * 论坛定位为学生端公开交流区，因此这里只接收标题、正文和可选图片，
 * 发帖人由当前登录学生身份自动确定，避免前端伪造用户信息。
 */
public record ForumPostRequest(
        @NotBlank String title,
        @NotBlank String content,
        String imagePath
) {
}
