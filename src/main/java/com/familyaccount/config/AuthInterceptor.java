package com.familyaccount.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familyaccount.common.JwtUtils;
import com.familyaccount.common.Result;
import com.familyaccount.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * JWT 登录拦截器
 * 拦截所有 /api/** 请求（除 /api/auth/**），校验 Token 并设置用户上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 放行预检请求（CORS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 获取 Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, 401, "未登录，请先登录");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            writeError(response, 401, "登录已过期，请重新登录");
            return false;
        }

        // 提取用户信息并设置上下文
        try {
            Long userId = jwtUtils.getUserIdFromToken(token);
            String username = jwtUtils.getUsernameFromToken(token);
            Long familyId = jwtUtils.getFamilyIdFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);
            Long memberId = jwtUtils.getMemberIdFromToken(token);

            UserContext.setUserId(userId);
            UserContext.setUsername(username);
            UserContext.setFamilyId(familyId);
            UserContext.setRole(role);
            UserContext.setMemberId(memberId);

            // 同时设置到 request attribute，供 Controller 使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            request.setAttribute("familyId", familyId);
            request.setAttribute("role", role);
            request.setAttribute("memberId", memberId);
        } catch (Exception e) {
            log.warn("解析 Token 用户信息失败: {}", e.getMessage());
            // 不阻断请求，但上下文可能为空
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，防止内存泄漏
        UserContext.clear();
    }

    /**
     * 返回 JSON 格式的 401 错误
     */
    private void writeError(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(Result.error(code, message)));
        writer.flush();
    }
}
