package com.familyaccount.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI 模块配置：创建带超时的 RestTemplate Bean
 */
@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final AiProperties aiProperties;

    @Bean
    public RestTemplate aiRestTemplate() {
        int timeout = Math.max(aiProperties.getTimeoutSeconds(), 10) * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }
}
