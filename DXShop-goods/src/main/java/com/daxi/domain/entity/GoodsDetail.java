package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 商品详情表DTO（富文本）
 */
@Data
@TableName("goods_detail")
public class GoodsDetail {
    /** 详情ID */
    @TableId
    private Long detailId;
    /** 商品SPU ID */
    private Long spuId;
    /** 详情富文本内容 */
    private String content;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
