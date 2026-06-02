package com.familyaccount.controller;

import com.familyaccount.common.Result;
import com.familyaccount.config.AiConfigManager;
import com.familyaccount.dto.AiChatRequest;
import com.familyaccount.dto.AiChatResponse;
import com.familyaccount.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@Tag(name = "AI助手", description = "自然语言记账与查询")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;
    private final AiConfigManager aiConfigManager;

    @Operation(summary = "发送消息给AI助手")
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = aiService.chat(request.getMessage());
        return Result.success(response);
    }

    @Operation(summary = "获取AI配置信息")
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        return Result.success(aiConfigManager.getConfigInfo());
    }

    @Operation(summary = "更新AI API Key（保存到外部配置文件）")
    @PutMapping("/config")
    public Result<Map<String, Object>> updateConfig(@RequestBody Map<String, String> body) {
        String apiKey = body.get("apiKey");
        if (apiKey == null) {
            return Result.error(400, "apiKey 不能为空");
        }
        try {
            if (apiKey.isBlank()) {
                // 清空：删除外部配置文件，回退到环境变量
                aiConfigManager.deleteExternalConfig();
                log.info("AI 外部配置文件已删除，回退至环境变量/application.yml");
            } else {
                aiConfigManager.saveApiKey(apiKey);
                log.info("AI API Key 已通过设置页更新");
            }
        } catch (IOException e) {
            log.error("保存 AI 配置文件失败", e);
            return Result.error(500, "保存配置文件失败: " + e.getMessage());
        }
        return Result.success(aiConfigManager.getConfigInfo());
    }
}
