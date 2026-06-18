package com.daxi.domain.dto;

import com.daxi.domain.bo.GoodsSkuSpecBO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsSkuToChangeDTO {
    @Data
    public static class SkuDTO {
        private Long skuId;
        private BigDecimal yuan;
        private Integer stock;
        private String barCode;
        private Integer sort;
        private String skuSpec;
        private Integer status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<Long> specIds;
    }

    private List<GoodsSkuSpecBO> specList;
    private List<SkuDTO> skuList;
}
