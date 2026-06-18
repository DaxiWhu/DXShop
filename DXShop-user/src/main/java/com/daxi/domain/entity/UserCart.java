package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_cart")
public class UserCart {

    /**
     * 购物车主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long cartId;

    /**
     * 所属用户ID
     */
    private Long userId;

    /**
     * 关联商品SKU ID
     */
    private Long skuId;

    /**
     * 关联商品SPU ID
     */
    private Long spuId;

    /**
     * 购买数量
     */
    private Integer buyNum;

    /**
     * 加入购物车时的商品价格
     */
    private BigDecimal price;

    /**
     * 商品名称
     */
    private String title;
    /**
     * 商品图片
     */
    private String mainImg;

    /**
     * 规格描述（如“红色 XL”）
     */
    private String skuSpec;

    /**
     * 勾选状态：1=已勾选 0=未勾选
     */
    private Integer checked;

    /**
     * 商品状态：1=正常 0=下架/失效
     */
    private Integer status;

    /**
     * 加购时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
