package com.daxi.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RefundTimeOutMessage implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    /** 退款ID */
    private Long refundId;
    
    /** 订单号 */
    private Long orderId;

    /** 用户ID */
    private Long userId;

    /** 店铺ID */
    private Long shopId;
}
