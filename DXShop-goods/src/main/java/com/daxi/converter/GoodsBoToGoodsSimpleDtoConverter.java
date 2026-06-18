package com.daxi.converter;

import com.daxi.domain.bo.GoodsBO;
import com.daxi.domain.bo.GoodsTagBO;
import com.daxi.domain.dto.GoodsSimpleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(uses = {com.daxi.converter.IntegerToBigDecimal.class,IntegerToBigDecimal.class}, componentModel = "spring")
public interface GoodsBoToGoodsSimpleDtoConverter {
    /**
     * BO → DTO 核心映射
     * 注意：
     *
     * 图片列表imgs自动由uses调用图片转换器
     */
    @Mapping(source = "tags", target = "tags", qualifiedByName = "tagListToString")
    GoodsSimpleDTO toDto(GoodsBO bo);

    /**
     * 商品列表转换
     */
    List<GoodsSimpleDTO> toDtoList(List<GoodsBO> boList);
    @Named("tagListToString")
    default String tagListToString(List<GoodsTagBO> tagList) {
        // 空列表返回空字符串
        if (CollectionUtils.isEmpty(tagList)) {
            return "";
        }
        // 提取 tagName → 过滤空值 → 逗号拼接
        return tagList.stream()
                .map(GoodsTagBO::getTagName)  // 提取标签名称
                .filter(Objects::nonNull)     // 过滤null
                .collect(Collectors.joining(",")); // 逗号分隔
    }

}
