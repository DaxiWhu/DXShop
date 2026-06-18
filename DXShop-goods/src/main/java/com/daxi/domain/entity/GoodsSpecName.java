package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 规格名称表DTO（如：颜色、内存、尺寸）
 */
@Data
@TableName("goods_spec_name")
public class GoodsSpecName {
    /** 规格名ID */
    private Long id;
    /** 商品SPU ID */
    private Long spuId;
    /** 规格名称（如：颜色、内存） */
    private String specName;
    /** 排序权重（数字越小越靠前） */
    private Integer sort;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
