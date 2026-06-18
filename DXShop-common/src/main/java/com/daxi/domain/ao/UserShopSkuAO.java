package com.daxi.domain.ao;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserShopSkuAO {
        /** SKU ID */
        private Long skuId;
        /** 售价 */
        private BigDecimal yuan;
        /** 库存 */
        private Integer stock;
        /** 条码 */
        private String barCode;
        /** 排序*/
        private Integer sort;
        /** 规格展示文案（如：黑色 256G）（不允许更新） */
        private String skuSpec;
        /** 状态：1=有效 0=无效 */
        private Integer status;
     /** 具体规格id*/
        private SpecRelation specs;
        @Data
        public static class SpecRelation{
            private Long skuId;
            private List<Long> specIds;
        }
}
