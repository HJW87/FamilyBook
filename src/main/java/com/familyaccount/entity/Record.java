package com.familyaccount.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("record")
public class Record {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属家庭ID */
    private Long familyId;

    /** 创建该记录的用户ID */
    private Long userId;

    /** 收支类型：INCOME / EXPENSE */
    private String type;

    /** 关联类别ID */
    private Long categoryId;

    /** 金额 */
    private BigDecimal amount;

    /** 家庭成员名称 */
    private String familyMember;

    /** 记账日期 */
    private LocalDate recordDate;

    /** 备注 */
    private String note;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
