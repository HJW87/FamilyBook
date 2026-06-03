package com.familyaccount.controller;

import com.familyaccount.common.Result;
import com.familyaccount.entity.FamilyMember;
import com.familyaccount.service.FamilyMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "家庭成员管理", description = "家庭成员的增删查")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class FamilyMemberController {

    private final FamilyMemberService familyMemberService;

    @Operation(summary = "获取成员列表")
    @GetMapping
    public Result<List<FamilyMember>> list() {
        return Result.success(familyMemberService.listMembers());
    }

    @Operation(summary = "添加成员")
    @PostMapping
    public Result<FamilyMember> add(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        return Result.success(familyMemberService.addMember(name));
    }

    @Operation(summary = "删除成员")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "成员ID") @PathVariable Long id) {
        familyMemberService.deleteMember(id);
        return Result.success();
    }
}
