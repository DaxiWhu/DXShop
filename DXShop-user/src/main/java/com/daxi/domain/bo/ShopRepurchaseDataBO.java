package com.daxi.domain.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShopRepurchaseDataBO {
    private Long shopId;
    private Long totalBuyUser;
    private Long firstBuyUser;
    private Long repurchaseUser;
    private Long totalOrderCnt;
    private Long repurchaseOrderCnt;
    private BigDecimal totalPayAmount;
}
