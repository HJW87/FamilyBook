package com.familyaccount.service;

import com.familyaccount.entity.Category;

import java.util.List;

public interface CategoryService {

    /** 获取所有类别（可选按类型过滤） */
    List<Category> listCategories(String type);

    /** 根据ID获取类别 */
    Category getById(Long id);

    /** 添加类别 */
    Category addCategory(Category category);

    /** 修改类别 */
    Category updateCategory(Long id, Category category);

    /** 删除类别（被记录引用时抛异常） */
    void deleteCategory(Long id);
}
