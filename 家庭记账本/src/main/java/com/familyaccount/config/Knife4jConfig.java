package com.familyaccount.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j (Swagger) API 文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("家庭记账本 API 文档")
                        .version("1.0.0")
                        .description("家庭日常财务收支管理系统接口文档")
                        .contact(new Contact()
                                .name("开发者")
                                .email("student@example.com")));
    }
}
