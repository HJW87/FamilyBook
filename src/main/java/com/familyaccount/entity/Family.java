package com.familyaccount.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 家庭
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("family")
public class Family {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String inviteCode;
    private Long adminId;
    private LocalDateTime createdAt;
}
