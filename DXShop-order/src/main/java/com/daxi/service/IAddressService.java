package com.daxi.service;


import com.daxi.domain.ao.UserAddressAO;
import com.daxi.domain.dto.UserAddressDTO;

import java.util.List;

public interface IAddressService {
    void addAddress(UserAddressAO userAddressAO);

    void deleteAddress(Long addressId, Long userId);

    List<UserAddressDTO> getAddresses(Long userId);
}
