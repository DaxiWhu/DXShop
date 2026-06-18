package com.daxi.converter;

import com.daxi.domain.bo.GoodsSkuBO;
import com.daxi.domain.bo.GoodsSkuSpecBO;
import com.daxi.domain.dto.GoodsSkuDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = {GoodsSkuSpecBoToDto.class,IntegerToBigDecimal.class}, componentModel = "spring")
public interface GoodsSkuBoToDto {
    @Mapping(source = "fen",target = "yuan")
    GoodsSkuDTO.SkuDTO toSkuDto(GoodsSkuBO.Sku sku);
    List<GoodsSkuDTO.SkuDTO> toSkuDtoList(List<GoodsSkuBO.Sku> skuList);

    GoodsSkuDTO.SkuSpecValueDTO toSkuSpecValueDto(GoodsSkuSpecBO.SkuSpecValue skuSpecValue);
    List<GoodsSkuDTO.SkuSpecValueDTO> toSkuSpecValueDtoList(List<GoodsSkuSpecBO.SkuSpecValue> skuSpecValueList);

    GoodsSkuDTO.SkuSpecDTO toSkuSpecDto(GoodsSkuSpecBO skuSpec);
    List<GoodsSkuDTO.SkuSpecDTO> toSkuSpecDtoList(List<GoodsSkuSpecBO> skuSpecList);

    GoodsSkuDTO toDto(GoodsSkuBO bo);
    List<GoodsSkuDTO> toDtoList(List<GoodsSkuBO> boList);
}
