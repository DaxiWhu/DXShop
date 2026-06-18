package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.UserShopInsertAO;
import com.daxi.domain.entity.GoodsParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper

public interface GoodsParamMapper extends BaseMapper<GoodsParam> {

    void insertSpuParam(@Param("spuId") Long spuId, @Param("list") List<UserShopInsertAO.CategoryAttrAO> ao);
}
