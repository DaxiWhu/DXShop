package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RefundRequestDetailDTO {
    private Long id;
    private String refundNo;
    private Long orderId;
    private Long userId;
    private Long shopId;
    private Integer refundType;
    private BigDecimal refundAmount;
    private String refundReason;
    private String evidenceImages;
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
}
