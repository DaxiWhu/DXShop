package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsSkuDTO {
    @Data
    public static class SkuSpecDTO {
        private Long specNameId;
        private String  specName;
        private List<SkuSpecValueDTO> specValues;
    }
    @Data
    public static class SkuSpecValueDTO {
        private String specValueId;
        private String specValue;
        private Integer status;
    }
    @Data
    public static class SkuDTO {
        private Long skuId;
        private BigDecimal yuan;
        private Integer stock;
        private String skuSpec;
        private List<Long> specIds;
    }

    private List<SkuSpecDTO> specList;
    private List<SkuDTO> skuList;
}
