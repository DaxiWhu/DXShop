package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_shop_follow")
public class UserShopFollow {

    /**
     * 关注主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关注用户ID
     */
    private Long userId;

    /**
     * 被关注
     */
    private Long shopId;

    /**
     * 关注状态：1=已关注 0=已取关
     */
    private Integer followStatus;

    /**
     * 关注时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
