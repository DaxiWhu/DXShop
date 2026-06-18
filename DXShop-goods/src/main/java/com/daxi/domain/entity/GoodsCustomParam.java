package com.daxi.domain.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商家自定义参数DTO
 */
@Data
@TableName("goods_custom_param")
public class GoodsCustomParam  {
    @TableId
    /** 主键ID */
    private Long id;
    /** 商品SPU ID */
    private Long spuId;
    /** 自定义属性名（如：产地、特色） */
    private String attrName;
    /** 自定义属性值（如：景德镇、纯手工） */
    private String attrValue;
    /** 排序权重 */
    private Integer sort;
}
