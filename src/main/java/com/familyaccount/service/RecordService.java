package com.familyaccount.service;

import com.familyaccount.common.PageResult;
import com.familyaccount.dto.RecordQueryDTO;
import com.familyaccount.entity.Record;

import java.util.Map;

public interface RecordService {

    /** 分页查询记录 */
    PageResult<Map<String, Object>> listRecords(RecordQueryDTO query);

    /** 根据ID获取记录 */
    Record getById(Long id);

    /** 添加记录 */
    Record addRecord(Record record);

    /** 修改记录 */
    Record updateRecord(Long id, Record record);

    /** 删除记录 */
    void deleteRecord(Long id);
}
