package com.daxi.domain.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserPrivateDTO {
    /**
     * 用户唯一ID
     */
    private Long userId;

    /**
     * 用户生日
     */
    private LocalDate birthday;

    /**
     * 绑定手机号（脱敏显示）
     */
    private String phone;

    /**
     * 绑定邮箱（脱敏显示）
     */
    private String email;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
