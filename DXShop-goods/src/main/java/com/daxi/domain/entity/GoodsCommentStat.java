package com.daxi.domain.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论标签统计表DTO（如：外观好看 80、质量不错 65）
 */
@Data
@TableName("goods_comment_stat")
public class GoodsCommentStat {
    /** 主键ID */
    private Long id;
    /** 商品SPU ID */
    private Long spuId;
    /** 标签名称（如：外观好看） */
    private String tagName;
    /** 统计数量 */
    private Integer count;
    /** 更新时间 */
    private LocalDateTime updatedAt;
}
