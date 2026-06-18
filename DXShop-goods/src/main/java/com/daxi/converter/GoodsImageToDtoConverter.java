package com.daxi.converter;

import com.daxi.domain.dto.GoodsImageDTO;
import com.daxi.domain.entity.GoodsImage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsImageToDtoConverter {
    GoodsImageDTO toDto(GoodsImage entity);
    List<GoodsImageDTO> toDtos(List<GoodsImage> entities);
}
