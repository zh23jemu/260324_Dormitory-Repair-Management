package com.dormrepair.dto.student;

import jakarta.validation.constraints.NotBlank;

/**
 * 论坛评论请求。
 * 评论内容由当前登录用户提交，前端只传递纯文本内容，评论人身份由登录上下文决定。
 */
public record ForumCommentRequest(
        @NotBlank String content
) {
}
