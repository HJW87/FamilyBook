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
 * 用户（登录账号）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户展示ID（如 100001），用于邀请、展示等场景 */
    private Long displayId;

    private String username;
    private String password;
    /** 用户头像：male=男 / female=女 / 文件名=自定义上传 */
    private String avatar;
    private LocalDateTime createdAt;
}
