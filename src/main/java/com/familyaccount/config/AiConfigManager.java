package com.familyaccount.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * AI 配置管理器 —— 支持从外部文件读取 API Key，并允许运行时更新。
 *
 * 优先级：外部文件 (./ai-config.properties) > 环境变量/application.yml > 空
 * 这样部署到云服务器时，只需在 JAR 同级目录创建 ai-config.properties 即可，
 * 无需设置系统环境变量。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiConfigManager {

    private static final Path CONFIG_FILE = Path.of("./ai-config.properties");
    private static final String KEY_API_KEY = "api-key";

    private final AiProperties aiProperties;

    /** 从外部配置文件加载的 API Key（null 表示文件不存在或无 key） */
    private volatile String externalApiKey;

    @PostConstruct
    public void init() {
        reloadFromFile();
        if (externalApiKey != null && !externalApiKey.isBlank()) {
            log.info("AI API Key 已从外部配置文件加载: {}", CONFIG_FILE.toAbsolutePath());
        }
    }

    // ==================== 公开方法 ====================

    /** 获取当前生效的 API Key（外部文件优先） */
    public String getEffectiveApiKey() {
        if (externalApiKey != null && !externalApiKey.isBlank()) {
            return externalApiKey;
        }
        return aiProperties.getApiKey();
    }

    /** 获取配置信息（前端展示用） */
    public Map<String, Object> getConfigInfo() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("enabled", aiProperties.isEnabled());
        info.put("model", aiProperties.getModel());

        String effectiveKey = getEffectiveApiKey();
        boolean configured = effectiveKey != null && !effectiveKey.isBlank();
        info.put("configured", configured);

        if (configured) {
            info.put("maskedKey", maskKey(effectiveKey));
        }

        // 判断来源
        if (externalApiKey != null && !externalApiKey.isBlank()) {
            info.put("source", "file");
            info.put("sourceDetail", "外部配置文件（" + CONFIG_FILE.toAbsolutePath() + "）");
        } else if (aiProperties.getApiKey() != null && !aiProperties.getApiKey().isBlank()) {
            info.put("source", "env");
            info.put("sourceDetail", "application.yml");
        } else {
            info.put("source", "none");
            info.put("sourceDetail", "未配置");
        }

        return info;
    }

    /** 保存 API Key 到外部文件（运行时更新，立即生效） */
    public synchronized void saveApiKey(String apiKey) throws IOException {
        Properties props = new Properties();
        if (apiKey != null && !apiKey.isBlank()) {
            props.setProperty(KEY_API_KEY, apiKey.trim());
        }
        // else: 空 key → 写入空文件，相当于清除外部配置

        try (OutputStream os = Files.newOutputStream(CONFIG_FILE)) {
            props.store(os, "AI Assistant API Key — auto-generated"
                    + "\n# Priority: this file > application.yml");
        }

        // 从文件重新加载以确认写入成功
        reloadFromFile();
        log.info("AI API Key 已保存到外部配置文件: {}", CONFIG_FILE.toAbsolutePath());
    }

    /** 删除外部配置文件，回退到环境变量/application.yml 中的 Key */
    public synchronized void deleteExternalConfig() throws IOException {
        Files.deleteIfExists(CONFIG_FILE);
        this.externalApiKey = null;
        log.info("外部 AI 配置文件已删除，回退到环境变量/application.yml");
    }

    // ==================== 内部方法 ====================

    private void reloadFromFile() {
        if (!Files.exists(CONFIG_FILE)) {
            this.externalApiKey = null;
            return;
        }
        try {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(CONFIG_FILE)) {
                props.load(is);
            }
            this.externalApiKey = props.getProperty(KEY_API_KEY);
            if (this.externalApiKey != null) {
                this.externalApiKey = this.externalApiKey.trim();
            }
        } catch (IOException e) {
            log.warn("读取外部 AI 配置文件失败: {}", e.getMessage());
            this.externalApiKey = null;
        }
    }

    private static String maskKey(String key) {
        if (key == null || key.length() <= 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }
}
