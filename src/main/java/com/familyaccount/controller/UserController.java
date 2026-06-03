package com.familyaccount.controller;

import com.familyaccount.common.Result;
import com.familyaccount.common.UserContext;
import com.familyaccount.entity.User;
import com.familyaccount.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Tag(name = "用户管理", description = "用户个人资料与头像")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${app.upload.avatar-dir:./uploads/avatars}")
    private String avatarDir;

    /** 获取头像存储目录的绝对路径（避免 Windows Tomcat 临时目录问题） */
    private Path getAvatarDir() {
        return Paths.get(avatarDir).toAbsolutePath();
    }

    @Operation(summary = "获取当前用户资料")
    @GetMapping("/profile")
    public Result<Map<String, Object>> profile() {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "displayId", user.getDisplayId(),
                "avatar", user.getAvatar() != null ? user.getAvatar() : "",
                "role", UserContext.getRole() != null ? UserContext.getRole() : ""
        ));
    }

    @Operation(summary = "更新头像（预设：male/female）")
    @PutMapping("/avatar")
    public Result<Map<String, String>> updateAvatar(@RequestBody Map<String, String> body) {
        String avatar = body.get("avatar");
        if (avatar == null || avatar.isBlank()) {
            return Result.badRequest("请选择头像类型");
        }
        Long userId = UserContext.getUserId();
        userService.updateAvatar(userId, avatar.trim());
        log.info("用户 {} 更新头像: {}", userId, avatar);
        return Result.success("头像更新成功", Map.of("avatar", avatar.trim()));
    }

    @Operation(summary = "上传自定义头像图片")
    @PostMapping("/avatar/upload")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.badRequest("请选择图片文件");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/"))) {
            return Result.badRequest("只支持上传图片文件");
        }

        // 限制大小（2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            return Result.badRequest("图片大小不能超过 2MB");
        }

        try {
            // 确保目录存在
            Path uploadPath = getAvatarDir();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            Long userId = UserContext.getUserId();
            String originalName = file.getOriginalFilename();
            String ext = ".png";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
                if (ext.length() > 5) ext = ".png"; // 防止过长的扩展名
            }
            String filename = "avatar_" + userId + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // 更新数据库
            userService.updateAvatar(userId, filename);
            log.info("用户 {} 上传自定义头像: {}", userId, filename);

            return Result.success("头像上传成功", Map.of("avatar", filename));
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.error(500, "头像上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "删除自定义头像（恢复默认）")
    @DeleteMapping("/avatar")
    public Result<Void> deleteAvatar() {
        Long userId = UserContext.getUserId();
        User user = userService.getById(userId);
        if (user != null && user.getAvatar() != null && !user.getAvatar().isBlank()
                && !"male".equals(user.getAvatar()) && !"female".equals(user.getAvatar())) {
            // 删除旧文件
            try {
                Path oldFile = getAvatarDir().resolve(user.getAvatar());
                Files.deleteIfExists(oldFile);
            } catch (IOException e) {
                log.warn("删除旧头像文件失败: {}", user.getAvatar());
            }
        }
        userService.updateAvatar(userId, null);
        return Result.success();
    }
}
