package com.familyaccount.dto;

import lombok.Data;

/**
 * 记录查询请求参数
 */
@Data
public class RecordQueryDTO {

    /** 页码（默认1） */
    private Integer page = 1;

    /** 每页条数（默认20） */
    private Integer size = 20;

    /** 收支类型：INCOME / EXPENSE */
    private String type;

    /** 类别ID */
    private Long categoryId;

    /** 家庭成员 */
    private String familyMember;

    /** 起始日期 yyyy-MM-dd */
    private String startDate;

    /** 截止日期 yyyy-MM-dd */
    private String endDate;

    /** 备注关键词 */
    private String keyword;

    /** 按创建用户ID筛选（用于个人账单统计） */
    private Long userId;
}
