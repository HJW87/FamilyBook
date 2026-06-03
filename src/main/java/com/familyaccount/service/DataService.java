package com.familyaccount.service;

import java.util.Map;

public interface DataService {

    /** 导出全部数据为 JSON Map */
    Map<String, Object> exportData();

    /** 导入数据（合并模式） */
    Map<String, Object> importData(Map<String, Object> data);
}
