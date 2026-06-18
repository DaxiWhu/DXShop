package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsSimpleDTO {
    private Long spuId;
    //简介
    private String subTitle;
    //主图url
    private String mainImg;
    //价格
    private BigDecimal price;
    //销量
    private Integer saleCount;
    //标签
    private String tags;
}
