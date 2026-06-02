package com.familyaccount.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.familyaccount.entity.*;
import com.familyaccount.mapper.*;
import com.familyaccount.service.FamilyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public Family createFamily(String name, Long userId) {
        // 生成8位随机邀请码
        String inviteCode = generateInviteCode();

        // 创建家庭
        Family family = Family.builder()
                .name(name)
                .inviteCode(inviteCode)
                .adminId(userId)
                .build();
        familyMapper.insert(family);

        // 为创建者创建管理员标签
        FamilyMember adminMember = FamilyMember.builder()
                .familyId(family.getId())
                .name("管理员")
                .userId(userId)
                .build();
        familyMemberMapper.insert(adminMember);

        // 为家庭复制预设类别
        copyPresetCategories(family.getId());

        return family;
    }

    @Override
    public Family getById(Long id) {
        return familyMapper.selectById(id);
    }

    @Override
    public List<Map<String, Object>> getMembers(Long familyId) {
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, familyId)
               .orderByAsc(FamilyMember::getId);
        List<FamilyMember> members = familyMemberMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (FamilyMember m : members) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", m.getId());
            map.put("name", m.getName());
            map.put("familyId", m.getFamilyId());
            map.put("userId", m.getUserId());
            if (m.getUserId() != null) {
                User user = userMapper.selectById(m.getUserId());
                map.put("username", user != null ? user.getUsername() : null);
                map.put("displayId", user != null ? user.getDisplayId() : null);
            } else {
                map.put("username", null);
                map.put("displayId", null);
            }
            map.put("createdAt", m.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    @Override
    public FamilyMember addMember(Long familyId, String name) {
        // 检查同家庭是否已有同名标签
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, familyId)
               .eq(FamilyMember::getName, name);
        if (familyMemberMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("该身份标签已存在");
        }

        FamilyMember member = FamilyMember.builder()
                .familyId(familyId)
                .name(name)
                .build();
        familyMemberMapper.insert(member);
        return member;
    }

    @Override
    public void deleteMember(Long memberId) {
        FamilyMember member = familyMemberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("该成员不存在");
        }
        // 只有空标签（未绑定用户）才能删除
        if (member.getUserId() != null) {
            throw new IllegalStateException("该标签已绑定用户，请先踢出用户再删除标签");
        }
        familyMemberMapper.deleteById(memberId);
    }

    @Override
    @Transactional
    public void inviteUser(Long memberId, Long displayId) {
        FamilyMember member = familyMemberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("该成员标签不存在");
        }
        if (member.getUserId() != null) {
            throw new IllegalStateException("该标签已绑定用户");
        }

        // 通过展示ID查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getDisplayId, displayId));
        if (user == null) {
            throw new IllegalArgumentException("用户ID '" + displayId + "' 不存在，请检查后重试");
        }

        // 检查用户是否已属于其他家庭（通过 family_member 表）
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, user.getId());
        if (familyMemberMapper.selectCount(memberWrapper) > 0) {
            throw new IllegalStateException("该用户已属于一个家庭，不能重复加入");
        }

        // 绑定用户到标签
        member.setUserId(user.getId());
        familyMemberMapper.updateById(member);
    }

    @Override
    public void kickUser(Long memberId) {
        FamilyMember member = familyMemberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("该成员不存在");
        }
        if (member.getUserId() == null) {
            throw new IllegalStateException("该标签没有绑定用户");
        }
        // 管理员不能被踢出
        Family family = familyMapper.selectById(member.getFamilyId());
        if (family != null && family.getAdminId().equals(member.getUserId())) {
            throw new IllegalStateException("管理员不能被踢出");
        }
        // 使用 LambdaUpdateWrapper 显式设置为 null，避免 MyBatis-Plus 忽略 null 字段
        LambdaUpdateWrapper<FamilyMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FamilyMember::getId, memberId)
                     .set(FamilyMember::getUserId, null);
        familyMemberMapper.update(null, updateWrapper);
        log.info("已将用户从标签 [{}] 踢出", member.getName());
    }

    @Override
    @Transactional
    public void joinFamily(Long userId, String inviteCode, String labelName) {
        // 查找家庭
        LambdaQueryWrapper<Family> familyWrapper = new LambdaQueryWrapper<>();
        familyWrapper.eq(Family::getInviteCode, inviteCode);
        Family family = familyMapper.selectOne(familyWrapper);
        if (family == null) {
            throw new IllegalArgumentException("邀请码无效");
        }

        // 检查用户是否已属于其他家庭
        LambdaQueryWrapper<FamilyMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(FamilyMember::getUserId, userId);
        if (familyMemberMapper.selectCount(memberWrapper) > 0) {
            throw new IllegalStateException("您已属于一个家庭，不能重复加入");
        }

        // 检查标签名在该家庭是否已存在
        LambdaQueryWrapper<FamilyMember> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(FamilyMember::getFamilyId, family.getId())
                   .eq(FamilyMember::getName, labelName);
        if (familyMemberMapper.selectCount(nameWrapper) > 0) {
            throw new IllegalArgumentException("该家庭中标签 '" + labelName + "' 已存在，请换一个名称");
        }

        // 创建成员标签并绑定
        FamilyMember member = FamilyMember.builder()
                .familyId(family.getId())
                .name(labelName)
                .userId(userId)
                .build();
        familyMemberMapper.insert(member);
        log.info("用户 [{}] 通过邀请码加入了家庭 [{}]，标签名=[{}]", userId, family.getName(), labelName);
    }

    @Override
    public void updateMemberName(Long memberId, Long userId, String newName) {
        FamilyMember member = familyMemberMapper.selectById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("该成员标签不存在");
        }
        // 只有该标签绑定的用户本人才能修改
        if (!userId.equals(member.getUserId())) {
            throw new IllegalStateException("只能修改自己的身份标签");
        }
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        newName = newName.trim();
        // 检查新名称是否与其他标签重复
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, member.getFamilyId())
               .eq(FamilyMember::getName, newName)
               .ne(FamilyMember::getId, memberId);
        if (familyMemberMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("该家庭中标签 '" + newName + "' 已存在");
        }
        member.setName(newName);
        familyMemberMapper.updateById(member);
        log.info("用户 [{}] 修改了自己的标签名称为 [{}]", userId, newName);
    }

    /**
     * 为家庭复制系统预设类别
     */
    private void copyPresetCategories(Long familyId) {
        String[][] presets = {
                {"工资", "INCOME", "💰", "10"},
                {"奖金", "INCOME", "🎁", "9"},
                {"兼职", "INCOME", "💻", "8"},
                {"理财", "INCOME", "📈", "7"},
                {"红包", "INCOME", "🧧", "6"},
                {"其他收入", "INCOME", "📦", "1"},
                {"餐饮", "EXPENSE", "🍔", "10"},
                {"交通", "EXPENSE", "🚗", "9"},
                {"购物", "EXPENSE", "🛒", "8"},
                {"居住", "EXPENSE", "🏠", "7"},
                {"娱乐", "EXPENSE", "🎮", "6"},
                {"医疗", "EXPENSE", "🏥", "5"},
                {"教育", "EXPENSE", "📚", "4"},
                {"人情", "EXPENSE", "🎉", "3"},
                {"通讯", "EXPENSE", "📱", "2"},
                {"其他支出", "EXPENSE", "📦", "1"},
        };

        for (String[] p : presets) {
            Category category = Category.builder()
                    .familyId(familyId)
                    .name(p[0])
                    .type(p[1])
                    .icon(p[2])
                    .sortOrder(Integer.parseInt(p[3]))
                    .build();
            categoryMapper.insert(category);
        }
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
