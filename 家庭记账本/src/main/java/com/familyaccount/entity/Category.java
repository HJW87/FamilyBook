package com.familyaccount.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属家庭ID（NULL=预设模板） */
    private Long familyId;

    /** 类别名称 */
    private String name;

    /** 收支类型：INCOME / EXPENSE */
    private String type;

    /** emoji 图标 */
    private String icon;

    /** 排序权重（越大越靠前） */
    private Integer sortOrder;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
