package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserCartSkuDTO {
    private Long spuId;
    private BigDecimal price;
    private String title;
    private String mainImg;
    private String skuSpec;
}
