package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserFavoritesSpuDTO {

    private Long spuId;

    private String title;

    private String subTitle;

    private String mainImg;

    private BigDecimal price;

    private Integer status;

    private LocalDateTime followTime;
}
