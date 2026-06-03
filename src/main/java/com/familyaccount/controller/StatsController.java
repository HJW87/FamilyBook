package com.familyaccount.controller;

import com.familyaccount.common.Result;
import com.familyaccount.dto.*;
import com.familyaccount.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "统计分析", description = "多维度收支统计分析")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "收支汇总")
    @GetMapping("/summary")
    public Result<StatsSummaryVO> summary(
            @Parameter(description = "时间维度：year/month/week/day/custom")
            @RequestParam(defaultValue = "month") String period,
            @Parameter(description = "自定义起始日期 yyyy-MM-dd")
            @RequestParam(required = false) String startDate,
            @Parameter(description = "自定义截止日期 yyyy-MM-dd")
            @RequestParam(required = false) String endDate,
            @Parameter(description = "按创建用户ID筛选（个人账单）")
            @RequestParam(required = false) Long userId) {
        return Result.success(statsService.getSummary(period, startDate, endDate, userId));
    }

    @Operation(summary = "按类别统计")
    @GetMapping("/by-category")
    public Result<List<CategoryStatsVO>> byCategory(
            @Parameter(description = "时间维度")
            @RequestParam(defaultValue = "month") String period,
            @Parameter(description = "收支类型：INCOME/EXPENSE")
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @Parameter(description = "按创建用户ID筛选（个人账单）")
            @RequestParam(required = false) Long userId) {
        return Result.success(statsService.getByCategory(period, type, startDate, endDate, userId));
    }

    @Operation(summary = "按家庭成员统计")
    @GetMapping("/by-member")
    public Result<List<MemberStatsVO>> byMember(
            @Parameter(description = "时间维度")
            @RequestParam(defaultValue = "month") String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @Parameter(description = "按创建用户ID筛选（个人账单）")
            @RequestParam(required = false) Long userId) {
        return Result.success(statsService.getByMember(period, startDate, endDate, userId));
    }

    @Operation(summary = "月度趋势")
    @GetMapping("/monthly-trend")
    public Result<List<MonthlyTrendVO>> monthlyTrend(
            @Parameter(description = "最近多少个月，默认12")
            @RequestParam(defaultValue = "12") Integer months,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @Parameter(description = "按创建用户ID筛选（个人账单）")
            @RequestParam(required = false) Long userId) {
        return Result.success(statsService.getMonthlyTrend(months, startDate, endDate, userId));
    }
}
