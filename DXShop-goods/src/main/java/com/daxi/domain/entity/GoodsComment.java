package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品评论表DTO
 */
@Data
@TableName("goods_comment")
public class GoodsComment {
    /** 评论ID */
    private Long commentId;
    /** 商品SPU ID */
    private Long spuId;
    /** 商品SKU ID */
    private Long skuId;
    /** 商品SKU 描述 */
    private String skuDsc;
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String userName;
    /** 用户头像 */
    private String userAvatar;
    /** 订单ID */
    private Long orderId;
    /** 评价内容 */
    private String content;
    /** 评分（1-5分） */
    private Integer score;
    /** 是否匿名：1=是 0=否 */
    private Integer isAnonymous;
    /** 评论图片（JSON数组格式） */
    private String pictures;
    /** 是否商品复购：1=是 0=否 */
    private Integer isRepurchase;
    /** 是否店铺回头客：1=是 0=否 */
    private Integer isShopReturnCustomer;
    /** 状态：1=显示 0=隐藏 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
