package com.daxi.converter;

import com.daxi.domain.dto.RefundRequestDetailDTO;
import com.daxi.domain.dto.RefundRequestSimpleDTO;
import com.daxi.domain.entity.OrderRefund;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefundToDto {
    RefundRequestSimpleDTO refundToSimpleDto(OrderRefund refund);
    RefundRequestDetailDTO refundToDetailDto(OrderRefund refund);
}
