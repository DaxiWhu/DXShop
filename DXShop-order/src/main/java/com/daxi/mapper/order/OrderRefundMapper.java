package com.daxi.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.ao.OrderRefundAO;
import com.daxi.domain.dto.RefundRequestSimpleDTO;
import com.daxi.domain.entity.OrderRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderRefundMapper extends BaseMapper<OrderRefund> {
    void insertNewRequest(@Param("request") OrderRefundAO request);

    List<RefundRequestSimpleDTO> selectRefundRequestsForUser(@Param("userId") Long userId,
                                                             @Param("status") Integer status,
                                                             @Param("offset") Integer offset,
                                                             @Param("pageSize") Integer pageSize);

    List<RefundRequestSimpleDTO> selectRefundRequestsForShop(@Param("shopId") Long shopId,
                                                             @Param("status") Integer status,
                                                             @Param("offset") Integer offset,
                                                             @Param("pageSize") Integer pageSize);

    List<RefundRequestSimpleDTO> selectRefundRequestByIds(@Param("ids") List<Long> ids);
}
