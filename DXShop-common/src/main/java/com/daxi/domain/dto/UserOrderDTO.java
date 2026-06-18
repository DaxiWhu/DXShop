package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserOrderDTO {
    // 订单ID
    private Long orderId;
    // 订单编号(全局唯一)
    private Long orderSn;
    // 用户ID
    private Long userId;
    // 店铺ID
    private Long shopId;
    // 商品SPU ID
    private Long spuId;
    // 商品SKU ID
    private Long skuId;
    // 商品名称快照
    private String goodsName;
    // 商品主图
    private String goodsImg;
    // 规格
    private String skuSpec;
    // 单价
    private BigDecimal perPrice;
    // 购买数量
    private Integer buyNum;
    // 实付金额
    private BigDecimal price;
    // 订单状态
    private Integer orderStatus;
    private Integer operateStatus;
    // 支付状态
    private Integer payStatus;
    // 支付时
    private LocalDateTime payTime;
    // 支付流水号
    private String paySn;
    // 创建时间
    private LocalDateTime createTime;
    // 备注
    private String remark;

    // 收货人姓名
    private String receiverName;
    // 收货人手机
    private String receiverPhone;
    // 收货地址
    private String receiverAddress;
    //是否更换地址
    private Integer addressStatus;
    // 物流公司
    private String logisticsCompany;
    // 运单号
    private String logisticsNo;
    // 发货时间
    private LocalDateTime sendTime;
    // 确认收货时间
    private LocalDateTime finishTime;
    // 是否评价
    private Integer isComment;
}
