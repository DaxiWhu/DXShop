package com.daxi.domain.bo;

import lombok.Data;

@Data
public class GoodsCategoryAttrBO {
    private Long id;
    private Long paramId;
    private Long spuId;
    private String attrName;
    private String attrValue;
    private Integer sort;
}
