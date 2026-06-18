package com.daxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.daxi.Exception.BusinessException;
import com.daxi.domain.ao.UserAddressAO;
import com.daxi.domain.dto.UserAddressDTO;
import com.daxi.domain.entity.UserAddress;
import com.daxi.mapper.order.UserAddressMapper;
import com.daxi.service.IAddressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.daxi.response.OrderResponse.ADDRESS_NOT_EXIST;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements IAddressService {
    private final @NonNull UserAddressMapper userAddressMapper;
    private final @NonNull StringRedisTemplate stringRedisTemplate;
    private final @NonNull AddressOperationService addressOperationService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(UserAddressAO userAddressAO) {
        if (userAddressAO.getAddressId() != null) {
            addressOperationService.updateAddress(userAddressAO);
        } else {
            addressOperationService.createAddress(userAddressAO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long addressId, Long userId) {
        UserAddress existingAddress = userAddressMapper.selectOne(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getAddressId, addressId)
                        .eq(UserAddress::getUserId, userId));
        if (existingAddress == null) {
            throw new BusinessException(ADDRESS_NOT_EXIST);
        }

        userAddressMapper.deleteById(addressId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAddressDTO> getAddresses(Long userId) {
        return userAddressMapper.getAddress(userId);
    }
}
