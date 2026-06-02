package com.familyaccount.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.familyaccount.entity.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface RecordMapper extends BaseMapper<Record> {

    /**
     * 多条件分页查询记录（关联类别信息）
     */
    Page<Map<String, Object>> selectRecordPage(Page<?> page,
                                                @Param("familyId") Long familyId,
                                                @Param("type") String type,
                                                @Param("categoryId") Long categoryId,
                                                @Param("familyMember") String familyMember,
                                                @Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                @Param("keyword") String keyword,
                                                @Param("userId") Long userId);
}
