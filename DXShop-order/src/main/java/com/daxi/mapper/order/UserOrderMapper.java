package com.daxi.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.dto.OrderStatusCountDTO;
import com.daxi.domain.dto.SendCommentDTO;
import com.daxi.domain.dto.UserOrderDTO;
import com.daxi.domain.dto.UserSimpleOrderDTO;
import com.daxi.domain.entity.UserOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserOrderMapper extends BaseMapper<UserOrder> {

    UserOrderDTO getDetailOrder(@Param("orderId") Long orderId,@Param("userId") Long userId);


    UserSimpleOrderDTO getSimpleOrderByOrderId(String orderId);

    List<UserSimpleOrderDTO> selectSimpleOrderPage(@Param("userId") Long userId,
                                                   @Param("status") Integer status,
                                                   @Param("offset") Integer offset,
                                                   @Param("pageSize") Integer pageSize);

    List<UserSimpleOrderDTO> selectSimpleOrderPageForShop(@Param("shopId") Long shopId,
                                                          @Param("status") Integer status,
                                                          @Param("offset") Integer offset,
                                                          @Param("pageSize") Integer pageSize);

    List<UserSimpleOrderDTO> selectSimpleOrderByIds(@Param("list") List<String> missIds);

    OrderStatusCountDTO countOrderByStatusForUser(@Param("userId") Long userId);

    OrderStatusCountDTO countOrderByStatusForShop(@Param("shopId") Long shopId);

    SendCommentDTO getInformationForSendComment(         @Param("userId") Long userId,
                                                         @Param("orderId") Long orderId,
                                                         @Param("spuId") Long spuId,
                                                         @Param("skuId") Long skuId);
}
