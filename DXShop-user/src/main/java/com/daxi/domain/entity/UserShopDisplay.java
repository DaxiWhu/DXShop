package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_shop_display")
public class UserShopDisplay {

    /**
     * 店铺ID（主键）
     * 注意：非自增，手动赋值/分布式ID
     */
    @TableId(type = IdType.INPUT)
    private Long shopId;

    private Long userId;
    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺类型：1-旗舰店 2-专卖店 3-专营店 4-个人店
     */
    private Integer shopType;

    /**
     * 店铺状态：0-关闭 1-正常 2-审核中 3-冻结
     */
    private Integer shopStatus;

    /**
     * 店铺logo的url地址
     */
    private String logoUrl;

    /**
     * 店铺评分
     */
    private BigDecimal shopScore;

    /**
     * 开店时间
     */
    private LocalDateTime createTime;
}
