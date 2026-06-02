package com.familyaccount.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 聊天请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {
    @NotBlank(message = "消息不能为空")
    private String message;
}
