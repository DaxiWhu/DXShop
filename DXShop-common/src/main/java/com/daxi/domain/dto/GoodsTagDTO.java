package com.daxi.domain.dto;

import lombok.Data;

@Data
public class GoodsTagDTO {
    /** 标签ID */
    private Long id;
    /** 标签名称（如：热销、新品、生日礼物） */
    private String tagName;
}
