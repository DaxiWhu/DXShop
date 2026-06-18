package com.daxi.converter;

import com.daxi.domain.dto.GoodsCustomParamDTO;
import com.daxi.domain.entity.GoodsCustomParam;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsCustomParamToDtoConverter {
    GoodsCustomParamDTO toDto(GoodsCustomParam entity);
    List<GoodsCustomParamDTO> toDtos(List<GoodsCustomParam> entity);
}
