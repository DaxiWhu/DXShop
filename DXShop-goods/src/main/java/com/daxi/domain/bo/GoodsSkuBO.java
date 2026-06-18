package com.daxi.domain.bo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsSkuBO {

    @Data
    public static class Sku {
        /** SKU ID */
        private Long skuId;
        /** 商品SPU ID */
        private Long spuId;
        /** 售价 */
        private Integer fen;
        /** 库存 */
        private Integer stock;
        /** 条码 */
        private String barCode;
        /** 排序*/
        private Integer sort;
        /** 规格展示文案（如：黑色 256G） */
        private String skuSpec;
        /** 状态：1=有效 0=无效 */
        private Integer status;
        /** 乐观锁（防超卖） */
        private Integer version;
        /** 创建时间 */
        private LocalDateTime createdAt;
        /** 更新时间 */
        private LocalDateTime updatedAt;
        /** 具体规格*/
        private List<Long> specIds;
    }

    private List<GoodsSkuSpecBO> specList;
    private List<Sku> skuList;

}
