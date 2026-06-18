package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品统计表DTO（收藏、点击、销量等）
 */
@Data
@TableName("goods_stat")
public class GoodsStat {
    /** 商品SPU ID */
    private Long spuId;
    /** 收藏数 */
    private Integer collectNum;
    /** 点击量 */
    private Long clickNum;
    /** 销量 */
    private Integer buyNum;
    /** 评论数 */
    private Integer commentNum;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
