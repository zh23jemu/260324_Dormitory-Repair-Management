package com.dormrepair.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record ServiceMessageReplyRequest(
        @NotBlank String replyContent
) {
}
