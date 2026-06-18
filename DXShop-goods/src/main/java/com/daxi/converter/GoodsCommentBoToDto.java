package com.daxi.converter;

import com.daxi.domain.bo.GoodsCommentBO;
import com.daxi.domain.dto.GoodsCommentDTO;
import com.daxi.domain.entity.GoodsComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GoodsCommentBoToDto {
    @Mapping(source = "updatedAt", target = "commentAt")
    GoodsCommentDTO.Comment toComment(GoodsComment comment);

    List<GoodsCommentDTO.Comment> toCommentList(List<GoodsComment> commentList);

    // 1. 嵌套字段映射：从BO的commentStat里取字段赋值给CommentTag
    @Mapping(source = "commentStat.id", target = "id")          // BO.commentStat.id → CommentTag.id
    @Mapping(source = "commentStat.tagName", target = "tagName")// BO.commentStat.tagName → CommentTag.tagName
    @Mapping(source = "commentStat.count", target = "count")    // BO.commentStat.count → CommentTag.count
    GoodsCommentDTO.CommentTag toCommentTag(GoodsCommentBO.GoodsCommentStatBO statBO);

    List<GoodsCommentDTO.CommentTag> toCommentTagList(List<GoodsCommentBO.GoodsCommentStatBO> statBOList);

    @Mapping(source ="commentList",target = "allComment")
    @Mapping(source ="commentStats",target = "commentTags")
    GoodsCommentDTO toGoodsCommentDTO(GoodsCommentBO goodsCommentBO);

    List<GoodsCommentDTO> toGoodsCommentDTOList(List<GoodsCommentBO> goodsCommentBOList);
}
