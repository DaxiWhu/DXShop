package com.daxi.converter;

import com.daxi.domain.dto.AddressModifyRequestDTO;
import com.daxi.domain.entity.OrderAddressModify;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderAddressModifyToDto {
    AddressModifyRequestDTO orderAddressModefyToDto(OrderAddressModify orderAddressModefy);
}
