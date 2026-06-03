package com.familyaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI 聊天响应（返回给前端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    /** 角色（固定 "ai"） */
    private String role;
    /** 自然语言回复内容 */
    private String content;
    /** 意图类型：add_record / query / chat / error */
    private String intent;
    /** 结构化数据（记录详情、统计结果等） */
    private Map<String, Object> data;
}
