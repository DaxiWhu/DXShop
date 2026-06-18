package com.daxi.domain.dto;

import lombok.Data;

@Data
public class GoodsSkuSpecDTO {
    /** 规格名ID */
    private Long nameId;
    /** 规格值ID */
    private Long valueId;
    /** 规格名称（如：颜色、内存） */
    private String specName;
    /** 规格值（如：黑色、256G） */
    private String specValue;
}
