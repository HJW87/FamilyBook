package com.familyaccount.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyaccount.common.UserContext;
import com.familyaccount.entity.Category;
import com.familyaccount.entity.FamilyMember;
import com.familyaccount.entity.Record;
import com.familyaccount.mapper.CategoryMapper;
import com.familyaccount.mapper.FamilyMemberMapper;
import com.familyaccount.mapper.RecordMapper;
import com.familyaccount.service.DataService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataServiceImpl implements DataService {

    private final CategoryMapper categoryMapper;
    private final RecordMapper recordMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> exportData() {
        Long familyId = UserContext.getFamilyId();

        // 只导出当前家庭数据
        LambdaQueryWrapper<Category> catWrapper = new LambdaQueryWrapper<>();
        if (familyId != null) catWrapper.eq(Category::getFamilyId, familyId);

        LambdaQueryWrapper<Record> recWrapper = new LambdaQueryWrapper<>();
        if (familyId != null) recWrapper.eq(Record::getFamilyId, familyId);

        LambdaQueryWrapper<FamilyMember> memWrapper = new LambdaQueryWrapper<>();
        if (familyId != null) memWrapper.eq(FamilyMember::getFamilyId, familyId);

        Map<String, Object> data = new HashMap<>();
        data.put("version", 2);
        data.put("exportedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        data.put("categories", categoryMapper.selectList(catWrapper));
        data.put("records", recordMapper.selectList(recWrapper));
        data.put("familyMembers", familyMemberMapper.selectList(memWrapper));

        log.info("数据导出成功 familyId={}: {} 个类别, {} 条记录, {} 个成员",
                familyId,
                ((List<?>) data.get("categories")).size(),
                ((List<?>) data.get("records")).size(),
                ((List<?>) data.get("familyMembers")).size());
        return data;
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> importData(Map<String, Object> data) {
        Long familyId = UserContext.getFamilyId();

        int importedCategories = 0;
        int importedRecords = 0;
        int importedMembers = 0;

        // 导入类别
        if (data.containsKey("categories")) {
            List<Map<String, Object>> categories = (List<Map<String, Object>>) data.get("categories");
            for (Map<String, Object> catMap : categories) {
                try {
                    Category cat = objectMapper.convertValue(catMap, Category.class);
                    // 检查是否已存在（按名称+类型+家庭去重）
                    LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(Category::getName, cat.getName())
                            .eq(Category::getType, cat.getType());
                    if (familyId != null) wrapper.eq(Category::getFamilyId, familyId);
                    if (categoryMapper.selectCount(wrapper) == 0) {
                        cat.setId(null);
                        cat.setCreatedAt(null);
                        cat.setFamilyId(familyId);
                        categoryMapper.insert(cat);
                        importedCategories++;
                    }
                } catch (Exception e) {
                    log.warn("导入类别失败: {}", e.getMessage());
                }
            }
        }

        // 导入成员
        if (data.containsKey("familyMembers")) {
            List<Map<String, Object>> members = (List<Map<String, Object>>) data.get("familyMembers");
            for (Map<String, Object> memMap : members) {
                try {
                    FamilyMember member = objectMapper.convertValue(memMap, FamilyMember.class);
                    LambdaQueryWrapper<FamilyMember> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(FamilyMember::getName, member.getName());
                    if (familyId != null) wrapper.eq(FamilyMember::getFamilyId, familyId);
                    if (familyMemberMapper.selectCount(wrapper) == 0) {
                        member.setId(null);
                        member.setCreatedAt(null);
                        member.setUserId(null); // 导入时不绑定用户
                        member.setFamilyId(familyId);
                        familyMemberMapper.insert(member);
                        importedMembers++;
                    }
                } catch (Exception e) {
                    log.warn("导入成员失败: {}", e.getMessage());
                }
            }
        }

        // 导入记录
        if (data.containsKey("records")) {
            List<Map<String, Object>> records = (List<Map<String, Object>>) data.get("records");
            for (Map<String, Object> recMap : records) {
                try {
                    Record record = objectMapper.convertValue(recMap, Record.class);
                    record.setId(null);
                    record.setCreatedAt(null);
                    record.setFamilyId(familyId);
                    recordMapper.insert(record);
                    importedRecords++;
                } catch (Exception e) {
                    log.warn("导入记录失败: {}", e.getMessage());
                }
            }
        }

        log.info("数据导入完成 familyId={}: {} 个类别, {} 条记录, {} 个成员",
                familyId, importedCategories, importedRecords, importedMembers);

        Map<String, Object> result = new HashMap<>();
        result.put("importedCategories", importedCategories);
        result.put("importedRecords", importedRecords);
        result.put("importedMembers", importedMembers);
        return result;
    }
}
