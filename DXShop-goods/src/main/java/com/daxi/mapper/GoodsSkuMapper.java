package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.UserShopSkuAO;
import com.daxi.domain.ao.UserShopSkuSpecAO;
import com.daxi.domain.bo.GoodsSkuBO;
import com.daxi.domain.bo.GoodsSkuSpecBO;
import com.daxi.domain.entity.GoodsSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper

public interface GoodsSkuMapper extends BaseMapper<GoodsSku> {

    GoodsSkuBO getSkuById(@Param("skuId") Long id);

    List<GoodsSkuBO.Sku> listSkuBySpuId(@Param("spuId") Long spuId);

    List<GoodsSkuSpecBO> listSkuSpecBySkuIds(@Param("skuIds") List<Long> ids);






    void updateSku(@Param("list") List<UserShopSkuAO> updateAos);

    void insertSku(@Param("spuId") Long spuId,@Param("list") List<UserShopSkuAO> addAos);

    void insertSkuSpecRelation(@Param("specs") List<UserShopSkuAO.SpecRelation> specs);

    List<GoodsSkuBO.Sku> listSkuToChangeBySpuId(@Param("spuId") Long spuId);

    List<GoodsSkuSpecBO> listSkuSpecToChangeBySkuIds(@Param("skuIds") List<Long> ids);
}
