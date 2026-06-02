package com.familyaccount.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Web MVC 配置：SPA 路由支持 + JWT 拦截器
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    /**
     * 注册 JWT 登录拦截器
     * 拦截 /api/** 除了 /api/auth/**（登录接口本身不需要认证）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",       // 所有认证接口（登录/注册/验证）
                        "/v3/api-docs/**",    // Knife4j API 文档
                        "/swagger-ui/**",
                        "/doc.html",
                        "/webjars/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 用户上传的头像文件（绝对路径避免 Tomcat 临时目录问题）
        String avatarPath = Paths.get("uploads", "avatars").toAbsolutePath().toString().replace("\\", "/");
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + avatarPath + "/");

        // 静态资源（CSS/JS/图片等）
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        // 如果静态文件存在，直接返回
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        // API 请求不处理
                        if (resourcePath.startsWith("api/")) {
                            return null;
                        }
                        // 其他请求全部返回 index.html（SPA 路由）
                        return new ClassPathResource("/static/index.html");
                    }
                });
    }
}
