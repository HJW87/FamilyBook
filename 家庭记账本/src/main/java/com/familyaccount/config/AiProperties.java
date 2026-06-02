package com.familyaccount.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 助手配置属性（通义千问 DashScope）
 */
@Data
@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {
    /** 是否启用 AI 助手 */
    private boolean enabled = true;
    /** DashScope API Key（从阿里云获取） */
    private String apiKey = "";
    /** API 基础地址 */
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    /** 模型名称：qwen-turbo / qwen-plus */
    private String model = "qwen-turbo";
    /** 最大输出 Token 数 */
    private int maxTokens = 1000;
    /** 温度（0-1），越低输出越确定 */
    private double temperature = 0.1;
    /** 请求超时时间（秒） */
    private int timeoutSeconds = 30;
}
