package com.familyaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 按类别统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsVO {

    /** 类别ID */
    private Long categoryId;

    /** 类别名称 */
    private String categoryName;

    /** 图标 */
    private String icon;

    /** 笔数 */
    private Long count;

    /** 金额合计 */
    private BigDecimal total;

    /** 占比（百分比，如 35.2 表示 35.2%） */
    private BigDecimal percentage;
}
