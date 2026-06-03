package com.familyaccount.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyaccount.common.UserContext;
import com.familyaccount.entity.FamilyMember;
import com.familyaccount.entity.Record;
import com.familyaccount.mapper.FamilyMemberMapper;
import com.familyaccount.mapper.RecordMapper;
import com.familyaccount.service.FamilyMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberMapper familyMemberMapper;
    private final RecordMapper recordMapper;

    @Override
    public List<FamilyMember> listMembers() {
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            return List.of();
        }
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, familyId);
        wrapper.orderByAsc(FamilyMember::getId);
        return familyMemberMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public FamilyMember addMember(String name) {
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            throw new IllegalStateException("请先创建或加入家庭");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("成员名称不能为空");
        }
        name = name.trim();
        // 检查同家庭是否重名
        LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyMember::getFamilyId, familyId)
               .eq(FamilyMember::getName, name);
        if (familyMemberMapper.selectCount(wrapper) > 0) {
            throw new IllegalArgumentException("成员 '" + name + "' 已存在");
        }
        FamilyMember member = FamilyMember.builder()
                .familyId(familyId)
                .name(name)
                .build();
        familyMemberMapper.insert(member);
        log.info("添加家庭成员成功: {}, familyId={}", name, familyId);
        return member;
    }

    @Override
    @Transactional
    public void deleteMember(Long id) {
        FamilyMember member = familyMemberMapper.selectById(id);
        if (member == null) {
            throw new IllegalArgumentException("成员不存在，ID: " + id);
        }
        // 检查是否绑定用户
        if (member.getUserId() != null) {
            throw new IllegalStateException("该成员已绑定用户，请先踢出用户再删除");
        }
        // 检查是否有记录引用此成员名称
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Record::getFamilyMember, member.getName());
        Long count = recordMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new IllegalStateException("该成员下有 " + count + " 条记录，无法删除。请先删除或迁移相关记录。");
        }
        familyMemberMapper.deleteById(id);
        log.info("删除家庭成员成功: {}", member.getName());
    }
}
