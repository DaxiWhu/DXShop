package com.daxi.domain.bo;

import lombok.Data;

/**
 * 标签主表DTO
 */
@Data
public class GoodsTagBO {
    /** 标签ID */
    private Long id;
    private Long spuId;
    /** 标签名称（如：热销、新品、生日礼物） */
    private String tagName;
    /** 排序权重 */
    private Integer sort;
}
