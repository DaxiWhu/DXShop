package com.daxi.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentStatusDTO {
    private Long orderId;
    private Integer payStatus;
    private String paySn;
    private LocalDateTime payTime;
    private String message;
}
