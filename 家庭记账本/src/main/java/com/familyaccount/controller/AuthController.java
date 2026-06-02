package com.familyaccount.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyaccount.common.JwtUtils;
import com.familyaccount.common.Result;
import com.familyaccount.entity.Family;
import com.familyaccount.entity.FamilyMember;
import com.familyaccount.entity.User;
import com.familyaccount.mapper.FamilyMapper;
import com.familyaccount.mapper.FamilyMemberMapper;
import com.familyaccount.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "登录认证", description = "用户注册/登录")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final FamilyMemberMapper familyMemberMapper;
    private final FamilyMapper familyMapper;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.trim().isEmpty()) {
            return Result.badRequest("请输入用户名");
        }
        if (password == null || password.trim().isEmpty()) {
            return Result.badRequest("请输入密码");
        }
        if (password.length() < 6) {
            return Result.badRequest("密码至少6位");
        }

        try {
            User user = userService.register(username.trim(), password);
            log.info("用户 [{}] 注册成功, displayId={}", username, user.getDisplayId());
            return Result.success("注册成功", Map.of(
                    "id", user.getId(),
                    "displayId", user.getDisplayId(),
                    "username", user.getUsername()
            ));
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return Result.badRequest("请输入用户名和密码");
        }

        try {
            User user = userService.login(username, password);

            // 查找用户的家庭成员信息
            Long familyId = null;
            Long memberId = null;
            String role = null;

            LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(FamilyMember::getUserId, user.getId());
            FamilyMember member = familyMemberMapper.selectOne(wrapper);
            if (member != null) {
                familyId = member.getFamilyId();
                memberId = member.getId();
                // 判断是否为管理员：检查 family.admin_id 是否等于当前用户ID
                Family family = familyMapper.selectById(familyId);
                if (family != null && family.getAdminId().equals(user.getId())) {
                    role = "ADMIN";
                } else {
                    role = "MEMBER";
                }
            }

            // 生成 JWT
            String token = jwtUtils.generateToken(user.getId(), user.getUsername(), familyId, role, memberId);
            log.info("用户 [{}] 登录成功, displayId={}, familyId={}, role={}, memberId={}",
                    username, user.getDisplayId(), familyId, role, memberId);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", user.getUsername());
            data.put("displayId", user.getDisplayId());
            data.put("userId", user.getId());
            data.put("familyId", familyId);
            data.put("memberId", memberId);
            data.put("role", role);
            data.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
            return Result.success("登录成功", data);
        } catch (IllegalArgumentException e) {
            log.warn("登录失败 [{}]: {}", username, e.getMessage());
            return Result.error(401, e.getMessage());
        }
    }

    @Operation(summary = "验证 Token 是否有效")
    @GetMapping("/verify")
    public Result<Map<String, Object>> verify(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateToken(token)) {
                String username = jwtUtils.getUsernameFromToken(token);
                Long userId = jwtUtils.getUserIdFromToken(token);
                Long familyId = jwtUtils.getFamilyIdFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);

                Map<String, Object> data = new HashMap<>();
                data.put("username", username);
                data.put("userId", userId);
                data.put("familyId", familyId);
                data.put("role", role);
                data.put("valid", true);
                return Result.success(data);
            }
        }
        return Result.error(401, "Token 无效或已过期");
    }
}
