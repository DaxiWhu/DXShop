package com.daxi.converter;

import com.daxi.domain.bo.GoodsBO;
import com.daxi.domain.bo.GoodsCategoryAttrBO;
import com.daxi.domain.bo.GoodsTagBO;
import com.daxi.domain.dto.GoodsToChangeDTO;
import com.daxi.domain.entity.GoodsCustomParam;
import com.daxi.domain.entity.GoodsImage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring",uses = IntegerToBigDecimal.class)
public interface GoodsBoToGoodsToChangeDtoConverter {
    GoodsToChangeDTO.ImageDTO toImageDto(GoodsImage goodsImage);
    List<GoodsToChangeDTO.ImageDTO> toImageDtoList(List<GoodsImage> goodsBOList);

    GoodsToChangeDTO.CategoryAttrDTO toAttrDto(GoodsCategoryAttrBO goodsAttrBO);
    List<GoodsToChangeDTO.CategoryAttrDTO> toAttrDtoList(List<GoodsCategoryAttrBO> goodsAttrBOList);

    GoodsToChangeDTO.CustomParamDTO toCustomParamDto(GoodsCustomParam goodsCustomParamDTO);
    List<GoodsToChangeDTO.CustomParamDTO> toCustomParamDtoList(List<GoodsCustomParam> goodsCustomParamDTOList);

    GoodsToChangeDTO.TagDTO toTagDto(GoodsTagBO goodsTagBO);
    List<GoodsToChangeDTO.TagDTO> toTagDtoList(List<GoodsTagBO> goodsTagBOList);

    GoodsToChangeDTO toDto(GoodsBO goodsBO);
    List<GoodsToChangeDTO> toDtoList(List<GoodsBO> goodsBOList);
}
