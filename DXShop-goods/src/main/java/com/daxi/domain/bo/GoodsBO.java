package com.daxi.domain.bo;

import com.daxi.domain.entity.GoodsCustomParam;
import com.daxi.domain.entity.GoodsImage;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsBO {
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
    /** 类目权重 */
    private Integer categoryWeight;
    /** 品牌名 */
    private String brandName;
    /** 商品标题 */
    private String title;
    /** 副标题/卖点 */
    private String subTitle;
    /** 主图URL */
    private String mainImg;
    /** 商品标签 */
    private List<GoodsTagBO> tags;
    /** 商品分类属性 */
    private List<GoodsCategoryAttrBO> categoryAttrs;
    /** 商品自定义属性 */
    private List<GoodsCustomParam> customAttrs;
    /** sku最低价格（供展示） */
    private Integer price;
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
    /** 排序权重（0~9999，数字越大越靠前） */
    private Integer sort;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 图URL */
    private List<GoodsImage> imgs;
}
