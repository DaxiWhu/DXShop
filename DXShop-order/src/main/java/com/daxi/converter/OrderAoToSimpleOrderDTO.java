package com.daxi.converter;


import com.daxi.domain.ao.UserOrderAO;
import com.daxi.domain.dto.UserSimpleOrderDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderAoToSimpleOrderDTO {
    UserSimpleOrderDTO orderAoToSimpleOrderDTO(UserOrderAO order);
    List<UserSimpleOrderDTO> orderAoToSimpleOrderDTOList(List<UserOrderAO> order);
}
