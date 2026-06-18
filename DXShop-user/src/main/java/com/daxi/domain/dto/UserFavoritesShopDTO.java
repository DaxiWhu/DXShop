package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserFavoritesShopDTO {
    private Long shopId;

    private String shopName;

    private Integer shopType;

    private Integer shopStatus;

    private String logoUrl;

    private BigDecimal shopScore;

    private LocalDateTime followTime;
}
