package com.familyaccount.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyaccount.common.UserContext;
import com.familyaccount.entity.Category;
import com.familyaccount.entity.Record;
import com.familyaccount.mapper.CategoryMapper;
import com.familyaccount.mapper.RecordMapper;
import com.familyaccount.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final RecordMapper recordMapper;

    @Override
    public List<Category> listCategories(String type) {
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            return List.of();
        }
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getFamilyId, familyId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Category::getType, type.toUpperCase());
        }
        wrapper.orderByDesc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new IllegalArgumentException("类别不存在，ID: " + id);
        }
        return category;
    }

    @Override
    @Transactional
    public Category addCategory(Category category) {
        // 注入家庭ID
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            throw new IllegalStateException("请先创建或加入家庭");
        }
        category.setFamilyId(familyId);

        // 校验类型
        if (!"INCOME".equals(category.getType()) && !"EXPENSE".equals(category.getType())) {
            throw new IllegalArgumentException("类别类型必须为 INCOME 或 EXPENSE");
        }
        // 默认图标
        if (category.getIcon() == null || category.getIcon().isEmpty()) {
            category.setIcon("📦");
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
        log.info("添加类别成功: {} ({}), familyId={}", category.getName(), category.getType(), familyId);
        return category;
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) {
        Category existing = getById(id);
        if (category.getName() != null) existing.setName(category.getName());
        if (category.getIcon() != null) existing.setIcon(category.getIcon());
        if (category.getSortOrder() != null) existing.setSortOrder(category.getSortOrder());
        // type 不允许修改
        categoryMapper.updateById(existing);
        log.info("修改类别成功: {}", existing.getName());
        return existing;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getById(id);
        // 检查是否有记录引用此类别
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getCategoryId, id);
        Long count = recordMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new IllegalStateException("该类别下有 " + count + " 条记录，无法删除。请先删除或迁移相关记录。");
        }
        categoryMapper.deleteById(id);
        log.info("删除类别成功: {}", category.getName());
    }
}
