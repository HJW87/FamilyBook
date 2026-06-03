package com.familyaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 收支汇总统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsSummaryVO {

    /** 总收入 */
    private BigDecimal totalIncome;

    /** 总支出 */
    private BigDecimal totalExpense;

    /** 结余 */
    private BigDecimal balance;

    /** 记录总数 */
    private Long recordCount;

    /** 时间范围标签（如"2026年6月"） */
    private String periodLabel;
}
