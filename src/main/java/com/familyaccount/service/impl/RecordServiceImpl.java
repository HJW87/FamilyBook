package com.familyaccount.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.familyaccount.common.PageResult;
import com.familyaccount.common.UserContext;
import com.familyaccount.dto.RecordQueryDTO;
import com.familyaccount.entity.Category;
import com.familyaccount.entity.Record;
import com.familyaccount.mapper.CategoryMapper;
import com.familyaccount.mapper.RecordMapper;
import com.familyaccount.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements RecordService {

    private final RecordMapper recordMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<Map<String, Object>> listRecords(RecordQueryDTO query) {
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            return PageResult.<Map<String, Object>>builder()
                    .records(List.of()).total(0L).page((long) query.getPage()).size((long) query.getSize()).build();
        }
        Page<Map<String, Object>> page = new Page<>(query.getPage(), query.getSize());
        Page<Map<String, Object>> result = recordMapper.selectRecordPage(
                page,
                familyId,
                query.getType(),
                query.getCategoryId(),
                query.getFamilyMember(),
                query.getStartDate(),
                query.getEndDate(),
                query.getKeyword(),
                query.getUserId()
        );
        return PageResult.<Map<String, Object>>builder()
                .records(result.getRecords())
                .total(result.getTotal())
                .page(result.getCurrent())
                .size(result.getSize())
                .build();
    }

    @Override
    public Record getById(Long id) {
        Record record = recordMapper.selectById(id);
        if (record == null) {
            throw new IllegalArgumentException("记录不存在，ID: " + id);
        }
        return record;
    }

    @Override
    @Transactional
    public Record addRecord(Record record) {
        // 注入当前用户的家庭ID和用户ID
        Long familyId = UserContext.getFamilyId();
        if (familyId == null) {
            throw new IllegalStateException("请先创建或加入家庭");
        }
        record.setFamilyId(familyId);
        record.setUserId(UserContext.getUserId());
        validateRecord(record);
        // 默认日期为今天
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        // 默认备注
        if (record.getNote() == null) {
            record.setNote("");
        }
        recordMapper.insert(record);
        log.info("添加记录成功: {} {}元, 成员: {}, familyId={}", record.getType(), record.getAmount(), record.getFamilyMember(), familyId);
        return record;
    }

    @Override
    @Transactional
    public Record updateRecord(Long id, Record record) {
        Record existing = getById(id);
        // 权限校验：只能修改自己创建的记录
        if (existing.getUserId() != null && !existing.getUserId().equals(UserContext.getUserId())) {
            throw new IllegalStateException("只能修改自己的账单记录");
        }
        if (record.getType() != null) existing.setType(record.getType());
        if (record.getCategoryId() != null) existing.setCategoryId(record.getCategoryId());
        if (record.getAmount() != null) existing.setAmount(record.getAmount());
        if (record.getFamilyMember() != null) existing.setFamilyMember(record.getFamilyMember());
        if (record.getRecordDate() != null) existing.setRecordDate(record.getRecordDate());
        if (record.getNote() != null) existing.setNote(record.getNote());
        validateRecord(existing);
        recordMapper.updateById(existing);
        log.info("修改记录成功: ID={}", id);
        return existing;
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        Record existing = getById(id);
        // 权限校验：只能删除自己创建的记录
        if (existing.getUserId() != null && !existing.getUserId().equals(UserContext.getUserId())) {
            throw new IllegalStateException("只能删除自己的账单记录");
        }
        recordMapper.deleteById(id);
        log.info("删除记录成功: ID={}", id);
    }

    /**
     * 记录数据校验
     */
    private void validateRecord(Record record) {
        if (!"INCOME".equals(record.getType()) && !"EXPENSE".equals(record.getType())) {
            throw new IllegalArgumentException("记录类型必须为 INCOME 或 EXPENSE");
        }
        if (record.getCategoryId() == null) {
            throw new IllegalArgumentException("请选择收支类别");
        }
        // 验证类别存在
        Category category = categoryMapper.selectById(record.getCategoryId());
        if (category == null) {
            throw new IllegalArgumentException("类别不存在，ID: " + record.getCategoryId());
        }
        if (record.getAmount() == null || record.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金额必须大于0");
        }
        if (record.getFamilyMember() == null || record.getFamilyMember().trim().isEmpty()) {
            throw new IllegalArgumentException("请选择或输入家庭成员");
        }
    }
}
