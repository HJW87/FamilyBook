package com.familyaccount.service;

import com.familyaccount.entity.Family;
import com.familyaccount.entity.FamilyMember;

import java.util.List;
import java.util.Map;

public interface FamilyService {

    /**
     * 创建家庭（自动创建管理员标签并复制预设类别）
     */
    Family createFamily(String name, Long userId);

    /**
     * 根据ID获取家庭
     */
    Family getById(Long id);

    /**
     * 获取家庭成员列表（含用户信息）
     */
    List<Map<String, Object>> getMembers(Long familyId);

    /**
     * 添加身份标签
     */
    FamilyMember addMember(Long familyId, String name);

    /**
     * 删除身份标签（仅当标签未绑定用户时）
     */
    void deleteMember(Long memberId);

    /**
     * 邀请用户加入家庭（通过用户展示ID绑定到标签）
     */
    void inviteUser(Long memberId, Long displayId);

    /**
     * 踢出用户（解绑用户，保留标签）
     */
    void kickUser(Long memberId);

    /**
     * 通过邀请码加入家庭（需自行填写身份标签名）
     */
    void joinFamily(Long userId, String inviteCode, String labelName);

    /**
     * 用户修改自己的身份标签名称
     */
    void updateMemberName(Long memberId, Long userId, String newName);
}
