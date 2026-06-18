package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.dto.UserFavoritesShopDTO;
import com.daxi.domain.entity.UserShopFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserShopFollowMapper extends BaseMapper<UserShopFollow> {

    List<UserFavoritesShopDTO> getMyFavoritesShops(@Param( "userId") Long userId);
}
