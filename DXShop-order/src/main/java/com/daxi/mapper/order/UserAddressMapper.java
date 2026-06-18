package com.daxi.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.dto.UserAddressDTO;
import com.daxi.domain.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    List<UserAddressDTO> getAddress(@Param("userId") Long userId);
}
