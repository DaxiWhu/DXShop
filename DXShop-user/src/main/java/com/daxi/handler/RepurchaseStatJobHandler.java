package com.daxi.handler;

import com.daxi.service.StaticService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 复购统计定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RepurchaseStatJobHandler {

    private final StaticService repurchaseStatService;

    /**
     * 店铺日复购统计任务
     * 任务参数：可选，格式 yyyy-MM-dd，不传则统计昨天数据
     */

    /**
     * 店铺日复购统计任务
     * 每天凌晨 1 点自动统计昨天数据
     */
    @XxlJob("shopDailyRepurchaseStatJob")
    public void shopDailyRepurchaseStatJob() {
        log.info("========== XXL-JOB：店铺日复购统计任务开始 ==========");

        LocalDate statDate = LocalDate.now().minusDays(1);
        log.info("统计日期：{}", statDate);

        repurchaseStatService.calculateShopDailyRepurchaseStat(statDate);

        log.info("========== XXL-JOB：店铺日复购统计任务完成 ==========");
    }

    /**
     * SPU日复购统计任务
     * 每天凌晨 2 点自动统计昨天数据
     */
    @XxlJob("spuDailyRepurchaseStatJob")
    public void spuDailyRepurchaseStatJob() {
        log.info("========== XXL-JOB：SPU日复购统计任务开始 ==========");

        LocalDate statDate = LocalDate.now().minusDays(1);
        log.info("统计日期：{}", statDate);

        repurchaseStatService.calculateSpuDailyRepurchaseStat(statDate);

        log.info("========== XXL-JOB：SPU日复购统计任务完成 ==========");
    }
}
