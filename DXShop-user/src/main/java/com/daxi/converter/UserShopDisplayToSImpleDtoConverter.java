package com.daxi.converter;

import com.daxi.domain.dto.UserShopSimpleDTO;
import com.daxi.domain.entity.UserShopDisplay;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserShopDisplayToSImpleDtoConverter {
    UserShopSimpleDTO toDto(UserShopDisplay userShopDisplay);
    List<UserShopSimpleDTO> toDtoList(List<UserShopDisplay> userShopDisplays);
}
