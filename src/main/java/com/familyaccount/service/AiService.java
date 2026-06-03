package com.familyaccount.service;

import com.familyaccount.dto.AiChatResponse;

/**
 * AI 助手服务
 */
public interface AiService {
    /** 处理用户自然语言消息，返回 AI 回复 */
    AiChatResponse chat(String userMessage);
}
