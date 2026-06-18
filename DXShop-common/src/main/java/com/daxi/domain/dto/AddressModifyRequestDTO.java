package com.daxi.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressModifyRequestDTO {
    private Long orderId;
    private Long userId;
    private Long shopId;
    private String oldName;
    private String oldPhone;
    private String oldDetail;
    private String newName;
    private String newPhone;
    private String newDetail;
    private Integer status;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
