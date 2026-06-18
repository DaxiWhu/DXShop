package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.entity.GoodsCommentStat;
import com.daxi.domain.entity.GoodsTagCommentRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@Mapper
public interface GoodsTagCommentRelationMapper extends BaseMapper<GoodsTagCommentRelation> {

    @Select("select * from goods_tag_comment_relation a where a.spu_id=#{id}")
    List<GoodsTagCommentRelation> getRelationById(Long id);
}
