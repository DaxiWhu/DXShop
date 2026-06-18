package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserSimpleOrderDTO {
    private Long orderId;
    private String goodsName;
    private String goodsImg;
    private String skuSpec;
    private BigDecimal perPrice;
    private Integer buyNum;
    private BigDecimal payAmount;
    private String status;
    private Integer operateStatus;
}
