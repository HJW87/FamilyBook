package com.familyaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 月度趋势统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendVO {

    /** 月份（yyyy-MM） */
    private String month;

    /** 收入合计 */
    private BigDecimal income;

    /** 支出合计 */
    private BigDecimal expense;
}
