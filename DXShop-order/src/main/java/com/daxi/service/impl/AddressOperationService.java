package com.daxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.daxi.Exception.BusinessException;
import com.daxi.domain.ao.UserAddressAO;
import com.daxi.domain.entity.UserAddress;
import com.daxi.mapper.order.UserAddressMapper;
import com.daxi.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.daxi.domain.ao.UserAddressAO.IS_DEFAULT;
import static com.daxi.domain.ao.UserAddressAO.IS_NOT_DEFAULT;
import static com.daxi.response.OrderResponse.PARAM_ERROR;
import static com.daxi.response.UserResponse.NOT_LOGIN;

@Service
@RequiredArgsConstructor
public class AddressOperationService {
    
    private final UserAddressMapper userAddressMapper;
    
    @Transactional(rollbackFor = Exception.class)
    public void createAddress(UserAddressAO userAddressAO) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        
        boolean exists = userAddressMapper.exists(
                new LambdaQueryWrapper<UserAddress>()
                        .eq(UserAddress::getAddressId, userAddressAO.getAddressId()));
        if (exists) {
            throw new BusinessException(PARAM_ERROR);
        }

        if (userAddressAO.getIsDefault() == IS_DEFAULT) {
            new LambdaUpdateChainWrapper<>(userAddressMapper)
                    .eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, IS_NOT_DEFAULT)
                    .update();
        }

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        userAddress.setReceiverName(userAddressAO.getReceiverName());
        userAddress.setReceiverPhone(userAddressAO.getReceiverPhone());
        userAddress.setProvince(userAddressAO.getProvince());
        userAddress.setCity(userAddressAO.getCity());
        userAddress.setDistrict(userAddressAO.getDistrict());
        userAddress.setDetailAddress(userAddressAO.getDetailAddress());
        userAddress.setIsDefault(userAddressAO.getIsDefault());
        userAddress.setTag(userAddressAO.getTag());

        userAddressMapper.insert(userAddress);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(UserAddressAO userAddressAO) {
        Long userId = UserUtil.getLocalUserId();
        UserAddress existingAddress = userAddressMapper.selectById(userAddressAO.getAddressId());
        if (existingAddress == null) {
            throw new BusinessException("地址不存在");
        }

        if (userAddressAO.getIsDefault() == IS_DEFAULT) {
            new LambdaUpdateChainWrapper<>(userAddressMapper)
                    .eq(UserAddress::getUserId, userId)
                    .ne(UserAddress::getAddressId, userAddressAO.getAddressId())
                    .set(UserAddress::getIsDefault, 0)
                    .update();
        }

        UserAddress userAddress = new UserAddress();
        userAddress.setAddressId(userAddressAO.getAddressId());
        userAddress.setReceiverName(userAddressAO.getReceiverName());
        userAddress.setReceiverPhone(userAddressAO.getReceiverPhone());
        userAddress.setProvince(userAddressAO.getProvince());
        userAddress.setCity(userAddressAO.getCity());
        userAddress.setDistrict(userAddressAO.getDistrict());
        userAddress.setDetailAddress(userAddressAO.getDetailAddress());
        userAddress.setIsDefault(userAddressAO.getIsDefault());
        userAddress.setTag(userAddressAO.getTag());

        userAddressMapper.updateById(userAddress);
    }
}
