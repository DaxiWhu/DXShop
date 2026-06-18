package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.bo.ShopRepurchaseDataBO;
import com.daxi.domain.entity.ShopDailyRepurchaseStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ShopDailyRepurchaseStatMapper extends BaseMapper<ShopDailyRepurchaseStat> {
    // 查询指定日期的店铺复购统计数据
    List<ShopRepurchaseDataBO> selectShopRepurchaseData(@Param("statDate") LocalDate statDate);

    // 批量插入或更新统计数据
    int batchInsertOrUpdate(@Param("list") List<ShopDailyRepurchaseStat> list);
}
