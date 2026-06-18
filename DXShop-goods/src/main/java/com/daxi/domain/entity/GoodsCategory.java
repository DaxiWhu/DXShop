package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品分类主表DTO
 */
@Data
@TableName("goods_category")
public class GoodsCategory {
    /** 分类ID（主键） */
    @TableId
    private Long categoryId;
    /** 父分类ID：0=顶级分类，>0=子分类 */
    private Long parentId;
    /** 分类名称（如：手机、数码、苹果手机） */
    private String categoryName;
    /** 分类层级：1=一级分类 2=二级分类 3=三级分类 */
    private Integer level;
    /** 排序权重（数字越大越靠前） */
    private Integer sort;
}
