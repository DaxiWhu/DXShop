package com.daxi.converter;

import com.daxi.domain.bo.GoodsBO;
import com.daxi.domain.dto.GoodsDetailDTO;
import com.daxi.domain.dto.GoodsImageDTO;
import com.daxi.domain.entity.GoodsImage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(uses = {GoodsAttrBoToDtoConverter.class,GoodsTagBoToDtoConverter.class,IntegerToBigDecimal.class}, componentModel = "spring")
public interface GoodsBoToGoodsDetailDtoConverter {
    GoodsImageDTO toImgDto(GoodsImage img);
    List<GoodsImageDTO> toImgDtoList(List<GoodsImage> imgList);

    GoodsDetailDTO toDto(GoodsBO goodsBO);
    List<GoodsDetailDTO> toDtoList(List<GoodsBO> boList);
}
