package com.daxi.domain.dto;

import lombok.Data;

@Data
public class GoodsCustomParamDTO {
    /** 自定义属性名（如：产地、特色） */
    private String attrName;
    /** 自定义属性值（如：景德镇、纯手工） */
    private String attrValue;
}
