package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 规格值表DTO（如：黑色、256G、XL）
 */
@Data
@TableName("goods_spec_value")
public class GoodsSpecValue {
    /** 规格值ID */
    private Long id;
    /** 关联规格名称表ID */
    private Long specId;
    /** 规格值（如：黑色、256G） */
    private String specValue;
    /** 排序权重（数字越小越靠前） */
    private Integer sort;
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
