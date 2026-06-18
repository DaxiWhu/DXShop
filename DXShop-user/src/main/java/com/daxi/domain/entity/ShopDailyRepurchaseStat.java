package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("shop_daily_repurchase_stat")
public class ShopDailyRepurchaseStat {
    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 统计日期 yyyy-MM-dd
     */
    private String statDate;

    /**
     * 当日有效购买总用户数
     */
    private Long totalBuyUser;

    /**
     * 当日店铺新客数(首购用户)
     */
    private Long firstBuyUser;

    /**
     * 当日店铺复购用户数(二次及以上)
     */
    private Long repurchaseUser;

    /**
     * 复购率=复购用户数/总购买用户数
     */
    private BigDecimal repurchaseRate;

    /**
     * 当日有效订单总数
     */
    private Long totalOrderCnt;

    /**
     * 当日复购订单数
     */
    private Long repurchaseOrderCnt;

    /**
     * 当日实付总金额
     */
    private BigDecimal totalPayAmount;

    /**
     * 统计创建时间
     */
    private LocalDateTime createTime;
}
