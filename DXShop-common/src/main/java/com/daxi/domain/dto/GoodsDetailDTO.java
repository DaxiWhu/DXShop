package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsDetailDTO {
    /** 商品ID */
    private Long spuId;
    /** 店铺ID */
    private Long shopId;
    /** 店铺名 */
    private String shopName;
    /** 类目ID */
    private Long categoryId;
    /** 类目名 */
    private String categoryName;
    /** 品牌名 */
    private String brandName;
    /** 商品标题 */
    private String title;
    /** 主图URL */
    private String mainImg;
    /** 商品标签 */
    private List<GoodsTagDTO> tags;
    /** 商品属性 */
    private List<GoodsCategoryAttrDTO> categoryAttrs;
    /** 商品自定义属性 */
    private List<GoodsCustomParamDTO> customAttrs;
    /** 图URL */
    private List<GoodsImageDTO> imgs;
    /** sku最低价格（供展示） */
    private BigDecimal price;
    /** 收藏量 */
    private Integer collectCount;
    /** 销量 */
    private Integer saleCount;
    /** 点击量*/
    private Long clickCount;
    /** 评论数 */
    private Integer commentCount;
    /** 状态：1=上架 2=下架 3=删除 */
    private Integer status;
    /** 是否热销：0=否 1=是 */
    private Integer isHot;
    /** 创建时间 */
    private LocalDateTime createdAt;

}
