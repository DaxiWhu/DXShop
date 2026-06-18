package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品图片表DTO
 */
@Data
@TableName("goods_image")
public class GoodsImage {
    /** 图片ID */
    @TableId
    private Long imgId;
    /** 商品SPU ID */
    private Long spuId;
    /** 图片URL */
    private String imgUrl;
    /** 图片描述 */
    private String description;
    /** 排序权重 */
    private Integer sort;
    /** 是否主图：1=是 0=否 */
    private Integer isMain;
}
