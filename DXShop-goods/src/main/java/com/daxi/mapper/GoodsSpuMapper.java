package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.UserShopInsertAO;
import com.daxi.domain.ao.UserShopSpuUpdateAO;
import com.daxi.domain.bo.GoodsBO;
import com.daxi.domain.bo.GoodsCategoryAttrBO;
import com.daxi.domain.dto.GoodsCategoryAttrDTO;
import com.daxi.domain.dto.GoodsDetailDTO;
import com.daxi.domain.dto.GoodsSimpleDTO;
import com.daxi.domain.dto.GoodsToChangeDTO;
import com.daxi.domain.entity.GoodsCustomParam;
import com.daxi.domain.entity.GoodsImage;
import com.daxi.domain.entity.GoodsSpu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper

public interface GoodsSpuMapper extends BaseMapper<GoodsSpu> {
    /**
     * 根据spuId列表批量查询商品基础信息（含分类、统计）
     * @param id spuId列表
     * @return 商品基础BO列表
     */
    GoodsDetailDTO getDetailGoodsById(@Param("id") Long id);

    List<GoodsBO> getGoodsToChangeByIds(List<Long> spuIds);

    List<GoodsImage> listImgsBySpuIds(@Param("spuIds") List<Long> spuIds);



    /**
     * 批量查询商品属性（按spuId分组，返回属性名+值）
     * @param spuId spuId列表
     * @return 属性数据
     */




    List<GoodsSimpleDTO> getGoodsSimpleByIds(@Param("ids") List<Long> ids);


    List<GoodsCategoryAttrDTO> listCategoryAttrsBySpuId(@Param("spuId") Long spuId);

    List<GoodsCategoryAttrBO> listCategoryAttrsBySpuIds(@Param("spuIds") List<Long> ids);

    List<GoodsCustomParam> listCustomParamsBySpuIds(@Param("spuIds") List<Long> ids);

    List<Long> countAllForUpdate(@Param("spuId") Long spuId);

    void insertSpu(@Param("ao") UserShopInsertAO ao);

    List<GoodsToChangeDTO> getGoodsToChangeByShopId(@Param("shopId") Long shopId,
                                                    @Param("offset") int offset,
                                                    @Param("pageSize") Integer pageSize);
}
