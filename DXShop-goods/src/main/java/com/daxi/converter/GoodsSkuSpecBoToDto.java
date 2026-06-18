package com.daxi.converter;

import com.daxi.domain.bo.GoodsSkuSpecBO;
import com.daxi.domain.dto.GoodsSkuSpecDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsSkuSpecBoToDto {
    GoodsSkuSpecDTO toDto(GoodsSkuSpecBO bo);
    List<GoodsSkuSpecDTO> toDtoList(List<GoodsSkuSpecBO> boList);
}
