package com.familyaccount.controller;

import com.familyaccount.common.Result;
import com.familyaccount.entity.Category;
import com.familyaccount.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "类别管理", description = "收支类别的增删改查")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取类别列表")
    @GetMapping
    public Result<List<Category>> list(
            @Parameter(description = "类型：INCOME/EXPENSE，不传返回全部")
            @RequestParam(required = false) String type) {
        return Result.success(categoryService.listCategories(type));
    }

    @Operation(summary = "添加类别")
    @PostMapping
    public Result<Category> add(@RequestBody Category category) {
        return Result.success(categoryService.addCategory(category));
    }

    @Operation(summary = "修改类别")
    @PutMapping("/{id}")
    public Result<Category> update(
            @Parameter(description = "类别ID") @PathVariable Long id,
            @RequestBody Category category) {
        return Result.success(categoryService.updateCategory(id, category));
    }

    @Operation(summary = "删除类别")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "类别ID") @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
