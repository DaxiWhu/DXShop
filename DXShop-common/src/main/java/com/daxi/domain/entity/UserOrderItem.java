package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_order_item")
public class UserOrderItem {
    // 关联订单ID
    private Long orderId;

    // 商品SPU ID
    private Long spuId;

    // 商品SKU ID
    private Long skuId;

    // 商品名称快照
    private String goodsName;

    // 商品主图
    private String goodsImg;

    // 规格
    private String skuSpec;

    // 单价
    private BigDecimal perPrice;

    // 购买数量
    private Integer buyNum;

    // 是否评价
    private Integer isComment;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 逻辑删除
    @TableLogic
    private Integer isDeleted;
}
