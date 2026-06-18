package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentPageDTO {
    private Long orderId;
    private String orderSn;
    private BigDecimal payAmount;
    private String paymentUrl;
    private String qrCode;
    private Integer expireMinutes;
}
