package com.daxi.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.UserOrderAO;
import com.daxi.domain.entity.UserOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserOrderItemMapper extends BaseMapper<UserOrderItem> {
     void createOrderItem(@Param("order") UserOrderAO order);

}
