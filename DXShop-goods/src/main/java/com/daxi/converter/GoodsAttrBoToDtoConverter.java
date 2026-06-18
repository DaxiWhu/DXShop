package com.daxi.converter;

import com.daxi.domain.bo.GoodsCategoryAttrBO;
import com.daxi.domain.dto.GoodsCategoryAttrDTO;
import com.daxi.domain.dto.GoodsCustomParamDTO;
import com.daxi.domain.entity.GoodsCustomParam;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsAttrBoToDtoConverter {
    GoodsCategoryAttrDTO toCategoryDto(GoodsCategoryAttrBO bo);
    List<GoodsCategoryAttrDTO> toCategoryDtoList(List<GoodsCategoryAttrBO> boList);
    GoodsCustomParamDTO toCustomParaDto(GoodsCustomParam bo);
    List<GoodsCustomParamDTO> toCustomParaDtoList(List<GoodsCustomParam> boList);
}
