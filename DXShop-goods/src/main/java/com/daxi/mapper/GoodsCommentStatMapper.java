package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.entity.GoodsCommentStat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper

public interface GoodsCommentStatMapper extends BaseMapper<GoodsCommentStat> {
    @Select("select * from goods_comment_stat where spu_id = #{id} order by count DESC")
    List<GoodsCommentStat>  getGoodsCommentStatById(Long id);
}
