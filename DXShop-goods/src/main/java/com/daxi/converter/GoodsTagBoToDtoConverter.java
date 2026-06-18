package com.daxi.converter;

import com.daxi.domain.bo.GoodsTagBO;
import com.daxi.domain.dto.GoodsTagDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsTagBoToDtoConverter {
    GoodsTagDTO toDto(GoodsTagBO bo);
    List<GoodsTagDTO> toDtoList(List<GoodsTagBO> boList);
}
