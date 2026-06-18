package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品参数值表DTO（系统属性的具体值）
 */
@Data
@TableName("goods_param")
public class GoodsParam {
    /** 参数值ID */
    private Long id;
    /** 商品SPU ID */
    private Long spuId;
    /** 关联参数模板ID */
    private Long templateId;
    /** 参数值（如：骁龙8 Gen3、5000mAh） */
    private String paramValue;
}
