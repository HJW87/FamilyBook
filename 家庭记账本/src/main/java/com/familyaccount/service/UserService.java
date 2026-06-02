package com.familyaccount.service;

import com.familyaccount.entity.User;

public interface UserService {

    /**
     * 注册新用户
     */
    User register(String username, String password);

    /**
     * 登录验证（返回用户实体，失败抛异常）
     */
    User login(String username, String password);

    /**
     * 根据ID获取用户
     */
    User getById(Long id);

    /**
     * 根据用户名查找用户
     */
    User getByUsername(String username);

    /**
     * 根据展示ID查找用户
     */
    User getByDisplayId(Long displayId);

    /**
     * 更新用户头像
     */
    void updateAvatar(Long userId, String avatar);
}
