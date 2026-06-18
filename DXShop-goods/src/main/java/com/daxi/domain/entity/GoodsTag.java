package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 标签主表DTO
 */
@Data
@TableName("goods_tag")
public class GoodsTag {
    /** 标签ID */
    private Long id;
    /** 标签名称（如：热销、新品、生日礼物） */
    private String tagName;
    /** 排序权重 */
    private Integer sort;
}
