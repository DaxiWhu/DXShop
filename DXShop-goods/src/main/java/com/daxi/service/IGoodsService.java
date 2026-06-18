package com.daxi.service;


import com.daxi.domain.ao.GetSpuPageAO;
import com.daxi.domain.ao.UserShopInsertAO;
import com.daxi.domain.ao.UserShopSpuUpdateAO;
import com.daxi.domain.dto.GoodsDetailDTO;
import com.daxi.domain.dto.GoodsSimpleDTO;
import com.daxi.domain.dto.GoodsToChangeDTO;

import java.util.List;

public interface IGoodsService {
    GoodsDetailDTO getDetailGoodsById(Long id);

    List<GoodsSimpleDTO> getGoodsSimpleByIds(List<Long> ids);

    List<GoodsToChangeDTO> getSpuIdByshopId(GetSpuPageAO ao);

    void updateSpu(Long spuId, UserShopSpuUpdateAO ao) throws Exception;


    void addSpu(UserShopInsertAO ao);
}
