package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.GoodsCommentAO;
import com.daxi.domain.entity.GoodsComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper

public interface GoodsCommentMapper extends BaseMapper<GoodsComment> {

    List<GoodsComment> getGoodsCommentById(Long spuId);

    void sendComment(@Param("ao") GoodsCommentAO ao);
}
