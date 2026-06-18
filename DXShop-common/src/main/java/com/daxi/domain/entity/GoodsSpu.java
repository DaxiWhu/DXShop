package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 商品SPU主表DTO
 */
@Data
@TableName("goods_spu")
public class GoodsSpu {
    /** 商品ID */
    @TableId
    private Long spuId;
    /** 店铺ID */
    private Long shopId;

    private String shopName;
    /** 类目ID */
    private Long categoryId;
    /** 品牌ID */
    private String brand;
    /** 商品标题 */
    private String title;
    /** 副标题/卖点 */
    private String subTitle;
    /** 主图URL */
    private String mainImg;
    /** sku最低价格（供展示） */
    private Integer price;
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
}
