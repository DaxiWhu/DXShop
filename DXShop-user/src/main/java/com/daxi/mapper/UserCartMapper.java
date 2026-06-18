package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.UserFollowSkuAO;
import com.daxi.domain.entity.UserCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserCartMapper extends BaseMapper<UserCart> {
    void addSkuToCart(@Param("ao") UserFollowSkuAO ao, @Param("userId") Long userId);
}
