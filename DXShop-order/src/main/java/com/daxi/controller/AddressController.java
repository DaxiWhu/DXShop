package com.daxi.controller;

import com.daxi.domain.ao.UserAddressAO;
import com.daxi.domain.dto.UserAddressDTO;
import com.daxi.result.Result;
import com.daxi.service.IAddressService;
import com.daxi.util.UserUtil;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.daxi.response.OrderResponse.ADDRESS_NOT_EXIST;
import static com.daxi.response.UserResponse.NOT_LOGIN;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {
    private final @NonNull IAddressService addressService;



    public static final int MIN_ID_SCALE = 1;
    /**用户修改地址或者新增1*/
    @PutMapping("")
    public Result<Void> changeAddress(
            @NonNull
            @Valid
            @RequestBody UserAddressAO addressAO) {
        addressService.addAddress(addressAO);
        return Result.success();
    }
    /**用户删除地址1*/
    @DeleteMapping("/{addressId}")
    public Result<Void> deleteAddress(Long addressId) {
        Long userId = UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        addressService.deleteAddress(addressId, userId);
        return Result.success();

    }
    /**用户查看地址1*/

    @GetMapping("")
    public Result<List<UserAddressDTO>> getAddresses() {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null){
            return Result.fail(NOT_LOGIN);
        }
        List<UserAddressDTO> addresses = addressService.getAddresses(userId);
        if(addresses==null){
            return Result.fail(ADDRESS_NOT_EXIST);
        }

        return Result.success();
    }
}
