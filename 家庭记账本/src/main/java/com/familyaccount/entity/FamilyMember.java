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
@TableName("family_member")
public class FamilyMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属家庭ID */
    private Long familyId;

    /** 身份标签名称（如: 爸爸、妈妈） */
    private String name;

    /** 绑定的用户ID（一对一，为空表示空座位） */
    private Long userId;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
