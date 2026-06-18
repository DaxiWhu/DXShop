package com.daxi.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserSpuPayDetail {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品SPU_ID
     */
    private Long spuId;

    /**
     * 所属店铺ID
     */
    private Long shopId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 是否SPU首购 1=是 0=否
     */
    private Byte isFirstBuy;

    /**
     * 该SPU实付金额
     */
    private BigDecimal payAmount;

    /**
     * 是否有效订单 1=有效支付 0=退款/取消
     */
    private Byte isValid;

    /**
     * 分区日期 yyyy-MM-dd
     */
    private String dt;
}
