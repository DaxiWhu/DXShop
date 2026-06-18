package com.daxi.domain.bo;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SpuRepurchaseDataBO {
    private Long spuId;
    private Long totalBuyUser;
    private Long firstBuyUser;
    private Long repurchaseUser;
    private Long totalOrderCnt;
    private BigDecimal totalPayAmount;
}
