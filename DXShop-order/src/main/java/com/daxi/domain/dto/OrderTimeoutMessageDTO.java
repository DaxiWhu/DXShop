package com.daxi.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订单超时消息 DTO
 * 用于 RocketMQ 延时消息检查订单支付状态
 */
@Data
public class OrderTimeoutMessageDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 订单号 */
    private Long orderId;
    
    /** 用户ID */
    private Long userId;
    
    /** 店铺ID */
    private Long shopId;
}
