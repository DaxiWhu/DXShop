package com.daxi.domain.bo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsSkuSpecBO {
    @Data
    public static class SkuSpecValue {
        private Long skuId;
        private Long specNameId;
        private Long specValueId;
        private String specValue;
        private Integer sort;
        private Integer status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
    /**
     * 规格名id
     * */
    private Long specNameId;
    /**
     * 规格名
     * */
    private String  specName;
    /**
     * 对应商品id
     * */
    private Long spuId;
    /**
     * 规格名排序先后权重
     * */
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /**
     * 规格值列表
     * */
    private List<SkuSpecValue> specValues;


}
