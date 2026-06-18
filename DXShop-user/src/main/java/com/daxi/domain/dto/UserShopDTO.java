package com.daxi.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserShopDTO {

    private Long shopId;
    private Long userId;

    private String shopName;

    private Integer shopType;

    private Integer shopStatus;

    private String logoUrl;

    private LocalDateTime createTime;


    private String shopDesc;

    private String businessHours;

    private String contactPhone;

    private String contactEmail;

    private String address;

}
