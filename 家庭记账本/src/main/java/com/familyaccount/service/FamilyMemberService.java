package com.familyaccount.service;

import com.familyaccount.entity.FamilyMember;

import java.util.List;

public interface FamilyMemberService {

    /** 获取所有家庭成员 */
    List<FamilyMember> listMembers();

    /** 添加成员 */
    FamilyMember addMember(String name);

    /** 删除成员（被记录引用时抛异常） */
    void deleteMember(Long id);
}
