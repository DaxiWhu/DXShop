package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.bo.SpuRepurchaseDataBO;
import com.daxi.domain.entity.SpuDailyRepurchaseStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SpuDailyRepurchaseStatMapper extends BaseMapper<SpuDailyRepurchaseStat> {
    // 查询指定日期的SPU复购统计数据
    List<SpuRepurchaseDataBO> selectSpuRepurchaseData(@Param("statDate") LocalDate statDate);

    // 批量插入或更新统计数据
    int batchInsertOrUpdate(@Param("list") List<SpuDailyRepurchaseStat> list);
}
