package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.dto.UserShopDTO;
import com.daxi.domain.entity.UserShopExtend;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserShopExtendMapper extends BaseMapper<UserShopExtend> {
    UserShopDTO getShopAllById(@Param("shopId") Long shopId);

    List<UserShopDTO> getShopAudit(@Param("startIndex") int index, @Param("pageSize") Integer pageSize);
}
