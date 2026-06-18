package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 分类属性模板DTO（系统固定属性，如手机：CPU、内存）
 */
@Data
@TableName("goods_category_param")
public class GoodsCategoryParam {
    /** 属性模板ID */
    @TableId
    private Long paramId;
    /** 分类ID */
    private Long categoryId;
    /** 属性名（如：CPU、内存、颜色） */
    private String paramName;
    /** 排序权重 */
    private Integer sort;
}
