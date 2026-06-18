package com.daxi.domain.dto;

import lombok.Data;

@Data
public class UserShopSimpleDTO {
    private Long shopId;
    private String shopName;
    private String logoUrl;
    private Integer shopStatus;
}
