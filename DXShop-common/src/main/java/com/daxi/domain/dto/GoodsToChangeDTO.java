package com.daxi.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsToChangeDTO {

    @Data
    public static class ImageDTO {
        /** 图片ID */
        private Long imgId;
        /** 图片URL */
        private String imgUrl;
        /** 图片描述 */
        private String description;
        /** 排序权重 */
        private Integer sort;
        /** 是否主图：1=是 0=否 */
        private Integer isMain;
    }
    @Data
    public static class TagDTO {
        /** 标签ID */
        private Long id;
        /** 标签名称（如：热销、新品、生日礼物） */
        private String tagName;
        /** 排序权重 */
        private Integer sort;
    }
    @Data
    public static class CategoryAttrDTO {
        /** 属性ID */
        private Long id;
        private String attrName;
        private String attrValue;
        private Integer sort;
    }
    @Data
    public static class CustomParamDTO {
        /** 属性ID */
        private Long id;
        private String attrName;
        private String attrValue;
        private Integer sort;
    }
    /** 商品ID */
    private Long spuId;
 /** 品牌名 */
    private String brand;
    /** 商品标题 */
    private String title;
    /** 副标题/卖点 */
    private String subTitle;
    /** 主图URL */
    private String mainImg;
    /** sku最低价格（供展示） */
    private BigDecimal price;
    /** 状态：1=上架 2=下架 3=删除 */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
    /** 图URL */
    private List<ImageDTO> imgs;
    /** 商品标签 */
    private List<TagDTO> tags;
    /** 商品属性 */
    private List<CategoryAttrDTO> categoryAttrs;
    /** 商品自定义属性 */
    private List<CustomParamDTO> customAttrs;
}
