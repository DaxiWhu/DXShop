package com.daxi.converter;

import com.daxi.domain.bo.GoodsCommentBO;
import com.daxi.domain.entity.GoodsCommentStat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsCommentStatToBo {
    @Mapping(target = "comments", ignore = true) // 忽略评论列表，后续Service手动赋值
    GoodsCommentBO.GoodsCommentStatBO toBo(GoodsCommentStat commentStat);
    List<GoodsCommentBO.GoodsCommentStatBO> toBoList(List<GoodsCommentStat> commentStats);
}
