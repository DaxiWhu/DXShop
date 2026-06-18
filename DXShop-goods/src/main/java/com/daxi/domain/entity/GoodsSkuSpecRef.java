package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * SKU规格关联DTO
 */
@Data
@TableName("goods_sku_spec_ref")
public class GoodsSkuSpecRef {
    /** 关联ID */
    private Long id;
    /** 商品SKU ID */
    private Long skuId;
    /** 关联规格值表ID */
    private Long specValueId;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
