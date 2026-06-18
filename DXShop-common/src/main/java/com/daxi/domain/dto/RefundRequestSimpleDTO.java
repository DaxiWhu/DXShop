package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class RefundRequestSimpleDTO {
    private Long id;
    private String refundNo;
    private Integer refundType;
    private BigDecimal refundAmount;
    private String refundReason;
    private Integer status;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private LocalDateTime refundTime;
    private String failReason;
}
