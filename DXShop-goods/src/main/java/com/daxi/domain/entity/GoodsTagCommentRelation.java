package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 评论-标签关联DTO
 */
@Data
@TableName("goods_tag_comment_relation")
public class GoodsTagCommentRelation {
    /** 主键ID */
    private Long id;
    /** 商品SPU ID */
    private Long spuId;
    /** 评论ID */
    private Long commentId;
    /** 标签ID */
    private Long tagId;
}
