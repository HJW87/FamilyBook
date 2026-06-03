package com.familyaccount.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyaccount.entity.User;
import com.familyaccount.mapper.UserMapper;
import com.familyaccount.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** 用户展示ID起始值 */
    private static final long DISPLAY_ID_START = 100000L;

    @Override
    public User register(String username, String password) {
        // 检查用户名唯一
        User existing = getByUsername(username);
        if (existing != null) {
            throw new IllegalArgumentException("用户名已被注册");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .build();
        userMapper.insert(user);

        // 生成展示ID：100000 + 自增ID，确保唯一
        user.setDisplayId(DISPLAY_ID_START + user.getId());
        userMapper.updateById(user);

        return user;
    }

    @Override
    public User login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public User getByDisplayId(Long displayId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDisplayId, displayId);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public void updateAvatar(Long userId, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setAvatar(avatar);
        userMapper.updateById(user);
    }
}
