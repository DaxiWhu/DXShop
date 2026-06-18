package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品-标签关联DTO
 */
@Data
@TableName("goods_tag_relation")
public class GoodsTagRelation {
    /** 主键ID */
    private Long id;
    /** 商品SPU ID */
    private Long spuId;
    /** 标签ID */
    private Long tagId;
}
