package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 商品SKU表DTO
 */
@Data
@TableName("goods_sku")
public class GoodsSku {
    /** SKU ID */
    private Long skuId;
    /** 商品SPU ID */
    private Long spuId;
    /** 售价 */
    private Integer price;
    /** 库存 */
    private Integer stock;
    /** 条码 */
    private String barCode;
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
}
