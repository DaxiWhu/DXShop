package com.daxi.service;


import com.daxi.domain.ao.UserShopSkuAO;
import com.daxi.domain.ao.UserShopSkuSpecAO;
import com.daxi.domain.ao.UserShopSpecNameAO;
import com.daxi.domain.bo.GoodsSkuBO;
import com.daxi.domain.dto.UserCartSkuDTO;

import java.util.List;

public interface IGoodsSkuService {
    GoodsSkuBO getSkuToChangeById(Long spuId);

    GoodsSkuBO getSkuChoice(Long spuId);

    void addSpecName(Long spuId, List<UserShopSpecNameAO> name);

    void addAndUpdateSkuSpec(Long spuId, UserShopSkuSpecAO ao);

    void addAndUpdateSku(Long spuId, List<UserShopSkuAO> aos);

    UserCartSkuDTO getSkuForUserCartById(Long skuId, Integer buyNum);
}
