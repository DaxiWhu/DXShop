package com.daxi.domain.ao;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserOrderReceiveAO {
    private Long userId;
    private Long shopId;
    private Long orderId;
    private Long spuId;
    private LocalDateTime payTime;
    private BigDecimal payAmount;

}
