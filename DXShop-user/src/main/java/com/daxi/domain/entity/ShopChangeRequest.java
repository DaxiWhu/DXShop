package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("shop_change_request")
public class ShopChangeRequest {
    private Long id;
    private Long shopId;
    private Long userId;
    private Integer shopType;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
