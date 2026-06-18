package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("user_display")
@Data
public class UserDisplay {
    /**
     * 用户ID，与user_private表一一对应（主键）
     */
    @TableId(type = IdType.INPUT)
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 性别：0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 用户头像URL
     */
    private String avatarUrl;

    /**
     * 账号状态：1-正常 2-冻结 3-注销
     */
    private Integer accountStatus;

    /**
     * 是否实名：0-未实名 1-已实名
     */
    private Integer isRealName;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
}
