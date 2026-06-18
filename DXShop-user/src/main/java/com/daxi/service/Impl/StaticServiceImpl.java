package com.daxi.service.Impl;

import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.daxi.domain.bo.ShopRepurchaseDataBO;
import com.daxi.domain.bo.SpuRepurchaseDataBO;
import com.daxi.domain.entity.ShopDailyRepurchaseStat;
import com.daxi.domain.entity.SpuDailyRepurchaseStat;
import com.daxi.domain.entity.UserShopPayDetail;
import com.daxi.domain.entity.UserSpuPayDetail;
import com.daxi.mapper.ShopDailyRepurchaseStatMapper;
import com.daxi.mapper.SpuDailyRepurchaseStatMapper;
import com.daxi.mapper.UserShopPayDetailMapper;
import com.daxi.mapper.UserSpuPayDetailMapper;
import com.daxi.service.StaticService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StaticServiceImpl implements StaticService {
    private final @NonNull UserShopPayDetailMapper userShopPayDetailMapper;
    private final @NonNull UserSpuPayDetailMapper userSpuPayDetailMapper;
    private final @NonNull ShopDailyRepurchaseStatMapper shopStatMapper;
    private final @NonNull SpuDailyRepurchaseStatMapper spuStatMapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void invalidateOrderData(Long orderId) {
        LambdaUpdateChainWrapper<UserShopPayDetail> updateShopChainWrapper = new LambdaUpdateChainWrapper<>(userShopPayDetailMapper);
        updateShopChainWrapper.eq(UserShopPayDetail::getOrderId, orderId)
                .set(UserShopPayDetail::getIsValid, 0).update();

        LambdaUpdateChainWrapper<UserSpuPayDetail> updateSpuChainWrapper = new LambdaUpdateChainWrapper<>(userSpuPayDetailMapper);
        updateSpuChainWrapper.eq(UserSpuPayDetail::getOrderId, orderId)
                .set(UserSpuPayDetail::getIsValid, 0).update();
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateShopDailyRepurchaseStat(LocalDate statDate) {
        log.info("【店铺复购统计】开始计算，日期：{}", statDate);

        // 1. 查询原始数据
        List<ShopRepurchaseDataBO> rawData = shopStatMapper.selectShopRepurchaseData(statDate);
        if (rawData == null || rawData.isEmpty()) {
            log.warn("【店铺复购统计】无数据，日期：{}", statDate);
            return;
        }

        log.info("【店铺复购统计】查询到 {} 条原始数据", rawData.size());

        // 2. 转换数据
        List<ShopDailyRepurchaseStat> statList = convertToShopStatList(rawData, statDate);

        // 3. 批量插入或更新
        int affectedRows = shopStatMapper.batchInsertOrUpdate(statList);
        log.info("【店铺复购统计】完成，处理 {} 条记录，影响行数：{}", statList.size(), affectedRows);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateSpuDailyRepurchaseStat(LocalDate statDate) {
        log.info("【SPU复购统计】开始计算，日期：{}", statDate);

        // 1. 查询原始数据
        List<SpuRepurchaseDataBO> rawData = spuStatMapper.selectSpuRepurchaseData(statDate);
        if (rawData == null || rawData.isEmpty()) {
            log.warn("【SPU复购统计】无数据，日期：{}", statDate);
            return;
        }

        log.info("【SPU复购统计】查询到 {} 条原始数据", rawData.size());

        // 2. 转换数据
        List<SpuDailyRepurchaseStat> statList = convertToSpuStatList(rawData, statDate);

        // 3. 批量插入或更新
        int affectedRows = spuStatMapper.batchInsertOrUpdate(statList);
        log.info("【SPU复购统计】完成，处理 {} 条记录，影响行数：{}", statList.size(), affectedRows);
    }

    /**
     * 转换店铺统计数据
     */
    private List<ShopDailyRepurchaseStat> convertToShopStatList(List<ShopRepurchaseDataBO> rawData, LocalDate statDate) {
        List<ShopDailyRepurchaseStat> list = new ArrayList<>();
        String statDateStr = statDate.format(DATE_FORMATTER);

        for (ShopRepurchaseDataBO data : rawData) {
            ShopDailyRepurchaseStat stat = new ShopDailyRepurchaseStat();

            Long shopId = data.getShopId();
            Long totalBuyUser = data.getTotalBuyUser();
            Long firstBuyUser = data.getFirstBuyUser();
            Long repurchaseUser = data.getRepurchaseUser();
            Long totalOrderCnt = data.getTotalOrderCnt();
            Long repurchaseOrderCnt = data.getRepurchaseOrderCnt();
            BigDecimal totalPayAmount = data.getTotalPayAmount();

            // 计算复购率：复购用户数 / 总购买用户数
            BigDecimal repurchaseRate = totalBuyUser != null && totalBuyUser > 0
                    ? BigDecimal.valueOf(repurchaseUser)
                    .divide(BigDecimal.valueOf(totalBuyUser), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            stat.setShopId(shopId);
            stat.setStatDate(statDateStr);
            stat.setTotalBuyUser(totalBuyUser);
            stat.setFirstBuyUser(firstBuyUser);
            stat.setRepurchaseUser(repurchaseUser);
            stat.setTotalOrderCnt(totalOrderCnt);
            stat.setRepurchaseOrderCnt(repurchaseOrderCnt);
            stat.setTotalPayAmount(totalPayAmount);
            stat.setRepurchaseRate(repurchaseRate);

            list.add(stat);
        }

        return list;
    }

    /**
     * 转换SPU统计数据
     */
    private List<SpuDailyRepurchaseStat> convertToSpuStatList(List<SpuRepurchaseDataBO> rawData, LocalDate statDate) {
        List<SpuDailyRepurchaseStat> list = new ArrayList<>();
        String statDateStr = statDate.format(DATE_FORMATTER);

        for (SpuRepurchaseDataBO data : rawData) {
            SpuDailyRepurchaseStat stat = new SpuDailyRepurchaseStat();

            Long spuId = data.getSpuId();
            Long totalBuyUser = data.getTotalBuyUser();
            Long firstBuyUser = data.getFirstBuyUser();
            Long repurchaseUser = data.getRepurchaseUser();
            Long totalOrderCnt = data.getTotalOrderCnt();
            BigDecimal totalPayAmount = data.getTotalPayAmount();

            // 计算复购率：复购用户数 / 总购买用户数
            BigDecimal repurchaseRate = totalBuyUser != null && totalBuyUser > 0
                    ? BigDecimal.valueOf(repurchaseUser)
                    .divide(BigDecimal.valueOf(totalBuyUser), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            stat.setSpuId(spuId);
            stat.setStatDate(statDateStr);
            stat.setTotalBuyUser(totalBuyUser);
            stat.setFirstBuyUser(firstBuyUser);
            stat.setRepurchaseUser(repurchaseUser);
            stat.setTotalOrderCnt(totalOrderCnt);
            stat.setTotalPayAmount(totalPayAmount);
            stat.setRepurchaseRate(repurchaseRate);

            list.add(stat);
        }

        return list;
    }
}
