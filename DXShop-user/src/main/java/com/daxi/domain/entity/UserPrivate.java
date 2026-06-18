package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("user_private")
@Data
public class UserPrivate {

    /**
     * 用户唯一ID，主键，与user_display表一一对应
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 用户生日
     */
    private LocalDate birthday;

    /**
     * 真实姓名（加密存储）
     */
    private String realName;

    /**
     * 身份证号（加密存储）
     */
    private String idCard;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP（支持IPv6）
     */
    private String lastLoginIp;

    /**
     * 绑定手机号（加密存储，非必须）
     */
    private String phone;

    /**
     * 绑定邮箱（非必须）
     */
    private String email;

    /**
     * 密码加盐哈希值（禁止明文存储）
     */
    private String passwordHash;


    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;
}
