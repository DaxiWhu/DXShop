package com.daxi.domain.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_refund")
public class OrderRefund {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String refundNo;
    private Long orderId;
    private Long userId;
    /**
     * 店铺ID
     */
    private Long shopId;
    //1-仅退款 2-退货退款
    private Integer refundType;
    private BigDecimal refundAmount;
    private String refundReason;
    private String evidenceImages;
    //1-待商家审核 2-待用户退货 3-待商家收货 4-退款成功 5-退款关闭 6-商家拒绝
    private Integer status;



    private LocalDateTime applyTime;
    private LocalDateTime auditTime;

    private LocalDateTime expireTime;

    private String returnWaybill;
    private String returnExpressCompany;
    private LocalDateTime receiveTime;
    private String refundChannel;
    private String refundNoChannel;
    private LocalDateTime refundTime;
    private String failReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
