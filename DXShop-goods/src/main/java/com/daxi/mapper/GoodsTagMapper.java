package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.UserShopSpuUpdateAO;
import com.daxi.domain.bo.GoodsTagBO;
import com.daxi.domain.dto.GoodsTagDTO;
import com.daxi.domain.entity.GoodsTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface GoodsTagMapper extends BaseMapper<GoodsTag> {
    List<GoodsTagDTO> listTagsBySpuId(@Param("spuId") Long spuId);

    List<GoodsTagBO> listTagsBySpuIds(@Param("spuIds") List<Long> spuIds);



}
