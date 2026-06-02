package com.familyaccount.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * AI 返回 JSON 的解析结构（内部使用，AI 输出的字段名可能略有差异）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiParsedIntent {

    /** 意图：add_record / query / chat */
    private String intent;

    // === add_record 字段 ===
    /** 收支类型：INCOME / EXPENSE */
    private String type;
    /** 类别名称（AI 从可用列表中选取） */
    private String categoryName;
    /** 金额 */
    private BigDecimal amount;
    /** 记录日期 yyyy-MM-dd（null=今天） */
    private String recordDate;
    /** 家庭成员名称 */
    @JsonProperty("familyMember")
    private String familyMember;
    /** 备注 */
    private String note;

    // === query 字段 ===
    /** 查询类型：summary / by_category / by_member / monthly_trend */
    private String queryType;
    /** 查询类别名称 */
    private String queryCategory;
    /** 查询成员名称 */
    private String queryMember;
    /** 查询范围：personal（个人，默认）/ family（全家） */
    private String queryScope;
    /** 时间周期：day / week / month / year */
    private String queryPeriod;
    /** 自定义起始日期 */
    private String queryStartDate;
    /** 自定义截止日期 */
    private String queryEndDate;

    // === chat 字段 ===
    /** 闲聊回复文本 */
    private String reply;
}
