package com.daxi.converter;

import com.daxi.domain.dto.RefundRequestDetailDTO;
import com.daxi.domain.entity.OrderRefund;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderRefundToRefundDetailDto {
    RefundRequestDetailDTO orderRefundToRefundRequestDetailDto(OrderRefund orderRefund);
}
