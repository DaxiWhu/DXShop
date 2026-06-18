package com.daxi.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单创建消息 DTO
 * 用于 RocketMQ 事务消息传递订单数据
 */
@Data
public class OrderCreateMessageDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 订单号 */
    private Long orderSn;
    
    /** 用户ID */
    private Long userId;
    
    /** 店铺ID */
    private Long shopId;
    
    /** SPU ID */
    private Long spuId;
    
    /** SKU ID */
    private Long skuId;
    
    /** 购买数量 */
    private Integer buyNum;
    
    /** 单价 */
    private BigDecimal perPrice;
    
    /** 总价 */
    private BigDecimal totalPrice;
    
    /** 收货人姓名 */
    private String receiverName;
    
    /** 收货人电话 */
    private String receiverPhone;
    
    /** 收货地址 */
    private String receiverAddress;
    
    /** 备注 */
    private String remark;
}
