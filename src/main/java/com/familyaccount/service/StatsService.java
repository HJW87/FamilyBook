package com.familyaccount.service;

import com.familyaccount.dto.*;

import java.util.List;

public interface StatsService {

    /** 收支汇总 */
    StatsSummaryVO getSummary(String period, String startDate, String endDate, Long userId);

    /** 按类别统计 */
    List<CategoryStatsVO> getByCategory(String period, String type, String startDate, String endDate, Long userId);

    /** 按成员统计 */
    List<MemberStatsVO> getByMember(String period, String startDate, String endDate, Long userId);

    /** 月度趋势 */
    List<MonthlyTrendVO> getMonthlyTrend(Integer months, String startDate, String endDate, Long userId);

    /** 根据时间维度计算日期范围 */
    DateRange calculateDateRange(String period, String startDate, String endDate);

    record DateRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {}
}
