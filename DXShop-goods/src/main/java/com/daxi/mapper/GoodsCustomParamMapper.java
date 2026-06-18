package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.UserShopSpuUpdateAO;
import com.daxi.domain.entity.GoodsCustomParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsCustomParamMapper extends BaseMapper<GoodsCustomParam>{
}
