package com.familyaccount.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 按家庭成员统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberStatsVO {

    /** 家庭成员名称 */
    private String familyMember;

    /** 收入合计 */
    private BigDecimal income;

    /** 支出合计 */
    private BigDecimal expense;

    /** 记录笔数 */
    private Long count;
}
