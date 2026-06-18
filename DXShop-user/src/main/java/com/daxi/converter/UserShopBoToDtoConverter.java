package com.daxi.converter;

import com.daxi.domain.bo.UserShopBO;
import com.daxi.domain.dto.UserShopDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserShopBoToDtoConverter {
    UserShopDTO toDto(UserShopBO bo);
    List<UserShopDTO> toDtoList(List<UserShopBO> boList);
}
