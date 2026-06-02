package com.familyaccount.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.familyaccount.common.UserContext;
import com.familyaccount.dto.*;
import com.familyaccount.entity.Category;
import com.familyaccount.entity.Record;
import com.familyaccount.mapper.CategoryMapper;
import com.familyaccount.mapper.RecordMapper;
import com.familyaccount.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final RecordMapper recordMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public StatsSummaryVO getSummary(String period, String startDate, String endDate, Long userId) {
        DateRange range = calculateDateRange(period, startDate, endDate);
        List<Record> records = getRecordsInRange(range, userId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Record r : records) {
            if ("INCOME".equals(r.getType())) {
                totalIncome = totalIncome.add(r.getAmount());
            } else {
                totalExpense = totalExpense.add(r.getAmount());
            }
        }

        return StatsSummaryVO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .recordCount((long) records.size())
                .periodLabel(buildPeriodLabel(period, range))
                .build();
    }

    @Override
    public List<CategoryStatsVO> getByCategory(String period, String type, String startDate, String endDate, Long userId) {
        DateRange range = calculateDateRange(period, startDate, endDate);
        List<Record> records = getRecordsInRange(range, userId);

        // 过滤类型
        if (type != null && !type.isEmpty()) {
            records = records.stream()
                    .filter(r -> type.equalsIgnoreCase(r.getType()))
                    .collect(Collectors.toList());
        }

        // 按类别分组统计
        Map<Long, List<Record>> grouped = records.stream()
                .collect(Collectors.groupingBy(Record::getCategoryId));

        // 计算总金额（用于算占比）
        BigDecimal grandTotal = records.stream()
                .map(Record::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 加载所有类别
        Map<Long, Category> categoryMap = categoryMapper.selectList(null)
                .stream().collect(Collectors.toMap(Category::getId, c -> c));

        List<CategoryStatsVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Record>> entry : grouped.entrySet()) {
            Long catId = entry.getKey();
            List<Record> catRecords = entry.getValue();
            BigDecimal total = catRecords.stream()
                    .map(Record::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal percentage = BigDecimal.ZERO;
            if (grandTotal.compareTo(BigDecimal.ZERO) > 0) {
                percentage = total.multiply(BigDecimal.valueOf(100))
                        .divide(grandTotal, 2, RoundingMode.HALF_UP);
            }
            Category cat = categoryMap.getOrDefault(catId, new Category());
            result.add(CategoryStatsVO.builder()
                    .categoryId(catId)
                    .categoryName(cat.getName())
                    .icon(cat.getIcon())
                    .count((long) catRecords.size())
                    .total(total)
                    .percentage(percentage)
                    .build());
        }

        // 按金额降序
        result.sort((a, b) -> b.getTotal().compareTo(a.getTotal()));
        return result;
    }

    @Override
    public List<MemberStatsVO> getByMember(String period, String startDate, String endDate, Long userId) {
        DateRange range = calculateDateRange(period, startDate, endDate);
        List<Record> records = getRecordsInRange(range, userId);

        // 按成员分组
        Map<String, List<Record>> grouped = records.stream()
                .collect(Collectors.groupingBy(Record::getFamilyMember));

        List<MemberStatsVO> result = new ArrayList<>();
        for (Map.Entry<String, List<Record>> entry : grouped.entrySet()) {
            String member = entry.getKey();
            List<Record> memberRecords = entry.getValue();
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;
            for (Record r : memberRecords) {
                if ("INCOME".equals(r.getType())) {
                    income = income.add(r.getAmount());
                } else {
                    expense = expense.add(r.getAmount());
                }
            }
            result.add(MemberStatsVO.builder()
                    .familyMember(member)
                    .income(income)
                    .expense(expense)
                    .count((long) memberRecords.size())
                    .build());
        }

        result.sort((a, b) -> b.getExpense().compareTo(a.getExpense()));
        return result;
    }

    @Override
    public List<MonthlyTrendVO> getMonthlyTrend(Integer months, String startDate, String endDate, Long userId) {
        if (months == null || months <= 0) months = 12;

        // 默认：最近 N 个月
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusMonths(months - 1).withDayOfMonth(1);

        // 如果传了自定义范围
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        }

        List<Record> records = getRecordsInRange(new DateRange(start, end), userId);

        // 按月分组
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, List<Record>> grouped = records.stream()
                .collect(Collectors.groupingBy(r -> r.getRecordDate().format(fmt),
                        TreeMap::new, Collectors.toList())); // TreeMap 保证月份有序

        List<MonthlyTrendVO> result = new ArrayList<>();
        for (Map.Entry<String, List<Record>> entry : grouped.entrySet()) {
            String month = entry.getKey();
            BigDecimal income = BigDecimal.ZERO;
            BigDecimal expense = BigDecimal.ZERO;
            for (Record r : entry.getValue()) {
                if ("INCOME".equals(r.getType())) {
                    income = income.add(r.getAmount());
                } else {
                    expense = expense.add(r.getAmount());
                }
            }
            result.add(MonthlyTrendVO.builder()
                    .month(month)
                    .income(income)
                    .expense(expense)
                    .build());
        }
        return result;
    }

    @Override
    public DateRange calculateDateRange(String period, String startDate, String endDate) {
        LocalDate now = LocalDate.now();
        LocalDate start;
        LocalDate end = now;

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            return new DateRange(LocalDate.parse(startDate), LocalDate.parse(endDate));
        }

        switch (period != null ? period.toLowerCase() : "month") {
            case "year":
                start = now.withDayOfYear(1);
                break;
            case "week":
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                break;
            case "day":
                start = now;
                break;
            case "month":
            default:
                start = now.withDayOfMonth(1);
                break;
        }
        return new DateRange(start, end);
    }

    /**
     * 获取指定日期范围内的所有记录
     * @param userId 可选，按创建用户ID筛选（用于个人账单统计）
     */
    private List<Record> getRecordsInRange(DateRange range, Long userId) {
        Long familyId = UserContext.getFamilyId();
        LambdaQueryWrapper<Record> wrapper = new LambdaQueryWrapper<>();
        if (familyId != null) {
            wrapper.eq(Record::getFamilyId, familyId);
        }
        if (userId != null) {
            wrapper.eq(Record::getUserId, userId);
        }
        wrapper.ge(Record::getRecordDate, range.startDate())
                .le(Record::getRecordDate, range.endDate())
                .orderByDesc(Record::getRecordDate);
        return recordMapper.selectList(wrapper);
    }

    private String buildPeriodLabel(String period, DateRange range) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy年M月d日");
        switch (period != null ? period.toLowerCase() : "month") {
            case "year": return range.startDate().getYear() + "年";
            case "month": return range.startDate().format(DateTimeFormatter.ofPattern("yyyy年M月"));
            case "week": return range.startDate().format(fmt) + " ~ " + range.endDate().format(fmt);
            case "day": return range.startDate().format(fmt);
            default: return range.startDate().format(fmt) + " ~ " + range.endDate().format(fmt);
        }
    }
}
