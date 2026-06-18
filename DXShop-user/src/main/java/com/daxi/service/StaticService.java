package com.daxi.service;

import java.time.LocalDate;

public interface StaticService {
    void invalidateOrderData(Long orderId);

    /**
     * 计算店铺日复购统计数据
     * @param statDate 统计日期
     */
    void calculateShopDailyRepurchaseStat(LocalDate statDate);

    /**
     * 计算SPU日复购统计数据
     * @param statDate 统计日期
     */
    void calculateSpuDailyRepurchaseStat(LocalDate statDate);

}
