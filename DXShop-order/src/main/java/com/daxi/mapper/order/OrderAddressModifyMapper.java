package com.daxi.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.UserOrderAddressAO;
import com.daxi.domain.dto.AddressModifyRequestDTO;
import com.daxi.domain.entity.OrderAddressModify;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderAddressModifyMapper extends BaseMapper<OrderAddressModify> {
    void createRequest(@Param("address") UserOrderAddressAO address);

    List<AddressModifyRequestDTO> selectAddressModifyRequestsForUser(@Param("userId") Long userId,
                                                                       @Param("status") Integer status,
                                                                       @Param("offset") Integer offset,
                                                                       @Param("pageSize") Integer pageSize);

    List<AddressModifyRequestDTO> selectAddressModifyRequestsForShop(@Param("shopId") Long shopId,
                                                                       @Param("status") Integer status,
                                                                       @Param("offset") Integer offset,
                                                                       @Param("pageSize") Integer pageSize);

    List<AddressModifyRequestDTO> selectAddressModifyRequestByIds(@Param("orderIds") List<String> orderIds);
}
