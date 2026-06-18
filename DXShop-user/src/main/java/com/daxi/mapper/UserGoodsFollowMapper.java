package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.dto.UserFavoritesSpuDTO;
import com.daxi.domain.entity.UserGoodsFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserGoodsFollowMapper extends BaseMapper<UserGoodsFollow> {
    void insertNewFollow(@Param("spuId") Long spuId, @Param("userId") Long userId);

    List<UserFavoritesSpuDTO> getMyFavoriteGoods(@Param("userId") Long userId);
}
