package com.familyaccount.controller;

import com.familyaccount.common.Result;
import com.familyaccount.common.UserContext;
import com.familyaccount.entity.Family;
import com.familyaccount.entity.FamilyMember;
import com.familyaccount.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "家庭管理", description = "家庭创建、成员管理、邀请/踢出")
@RestController
@RequestMapping("/api/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @Operation(summary = "创建家庭")
    @PostMapping("/create")
    public Result<Map<String, Object>> createFamily(@RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return Result.badRequest("请输入家庭名称");
        }

        try {
            Family family = familyService.createFamily(name.trim(), userId);
            return Result.success("家庭创建成功", Map.of(
                    "id", family.getId(),
                    "name", family.getName(),
                    "inviteCode", family.getInviteCode()
            ));
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    @Operation(summary = "通过邀请码加入家庭（需自行填写身份标签）")
    @PostMapping("/join")
    public Result<Void> joinFamily(@RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        String inviteCode = body.get("inviteCode");
        String labelName = body.get("labelName");

        if (inviteCode == null || inviteCode.trim().isEmpty()) {
            return Result.badRequest("请输入邀请码");
        }
        if (labelName == null || labelName.trim().isEmpty()) {
            return Result.badRequest("请输入您的身份标签（如：爸爸、妈妈、儿子）");
        }

        try {
            familyService.joinFamily(userId, inviteCode.trim(), labelName.trim());
            return Result.success();
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "获取家庭成员列表")
    @GetMapping("/members")
    public Result<List<Map<String, Object>>> getMembers() {
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            return Result.success("尚未加入家庭", List.of());
        }
        return Result.success(familyService.getMembers(familyId));
    }

    @Operation(summary = "添加身份标签（仅管理员）")
    @PostMapping("/members")
    public Result<FamilyMember> addMember(@RequestBody Map<String, String> body) {
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            return Result.error(400, "请先创建或加入家庭");
        }

        // 检查是否为管理员
        if (!"ADMIN".equals(UserContext.getRole())) {
            return Result.error(403, "仅管理员可添加标签");
        }

        String name = body.get("name");
        if (name == null || name.trim().isEmpty()) {
            return Result.badRequest("请输入标签名称");
        }

        try {
            FamilyMember member = familyService.addMember(familyId, name.trim());
            return Result.success(member);
        } catch (IllegalArgumentException e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "删除身份标签（仅管理员可删空标签）")
    @DeleteMapping("/members/{id}")
    public Result<Void> deleteMember(@PathVariable Long id) {
        // 检查是否为管理员
        if (!"ADMIN".equals(UserContext.getRole())) {
            return Result.error(403, "仅管理员可删除标签");
        }

        try {
            familyService.deleteMember(id);
            return Result.success();
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "修改自己的身份标签名称")
    @PutMapping("/members/{id}/name")
    public Result<Void> updateMemberName(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }

        String newName = body.get("name");
        if (newName == null || newName.trim().isEmpty()) {
            return Result.badRequest("标签名称不能为空");
        }

        try {
            familyService.updateMemberName(id, userId, newName.trim());
            return Result.success();
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "邀请用户加入家庭（通过用户展示ID，仅管理员）")
    @PostMapping("/invite")
    public Result<Void> inviteUser(@RequestBody Map<String, Object> body) {
        // 检查是否为管理员
        if (!"ADMIN".equals(UserContext.getRole())) {
            return Result.error(403, "仅管理员可邀请成员");
        }

        Long memberId = body.get("memberId") != null ? Long.valueOf(body.get("memberId").toString()) : null;
        Long displayId = body.get("displayId") != null ? Long.valueOf(body.get("displayId").toString()) : null;

        if (memberId == null || displayId == null) {
            return Result.badRequest("请提供 memberId 和 displayId");
        }

        try {
            familyService.inviteUser(memberId, displayId);
            return Result.success();
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "踢出用户（仅管理员）")
    @PostMapping("/kick/{id}")
    public Result<Void> kickUser(@PathVariable Long id) {
        // 检查是否为管理员
        if (!"ADMIN".equals(UserContext.getRole())) {
            return Result.error(403, "仅管理员可踢出成员");
        }

        try {
            familyService.kickUser(id);
            return Result.success();
        } catch (Exception e) {
            return Result.badRequest(e.getMessage());
        }
    }

    @Operation(summary = "获取家庭信息")
    @GetMapping("/info")
    public Result<Family> getFamilyInfo() {
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            return Result.error(400, "尚未加入家庭");
        }
        Family family = familyService.getById(familyId);
        return Result.success(family);
    }
}
