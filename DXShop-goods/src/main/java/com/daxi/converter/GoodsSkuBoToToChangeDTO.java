package com.daxi.converter;

import com.daxi.domain.bo.GoodsSkuBO;
import com.daxi.domain.dto.GoodsSkuToChangeDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = IntegerToBigDecimal.class, componentModel = "spring")
public interface GoodsSkuBoToToChangeDTO {
    GoodsSkuToChangeDTO.SkuDTO toSkuDto(GoodsSkuBO.Sku sku);
    List<GoodsSkuToChangeDTO.SkuDTO> toSkuDtoList(List<GoodsSkuBO.Sku> skuList);
    GoodsSkuToChangeDTO toDto(GoodsSkuBO bo);
    List<GoodsSkuToChangeDTO> toDtoList(List<GoodsSkuBO> boList);
}
