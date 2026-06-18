package com.daxi.service;

import com.daxi.domain.ao.GetShopCreateRequestPageAO;
import com.daxi.domain.ao.UserShopAO;
import com.daxi.domain.ao.UserShopCreateAO;
import com.daxi.domain.dto.LoginResponseDTO;
import com.daxi.domain.dto.UserShopDTO;
import com.daxi.domain.dto.UserShopSimpleDTO;
import com.daxi.domain.entity.ShopChangeRequest;
import com.daxi.domain.entity.UserShopDisplay;
import jakarta.validation.Valid;

import java.util.List;

public interface UserShopService {
    String updateShop(Long shopId, UserShopAO ao) ;

    UserShopDisplay getShopShowById(Long shopId);

    UserShopDTO getShopAllById(Long shopId);

    List<UserShopSimpleDTO> getMyShopByuserId(Long userId);

    void createShop(UserShopCreateAO ao, Long userId);

    LoginResponseDTO loginShop(Long shopId, Long userId);

    void auditShop(Long shopId, Integer result);

    List<UserShopDTO> getShopAudit(@Valid GetShopCreateRequestPageAO ao);

    List<ShopChangeRequest> getShopChangeRequest(Long shopId);

    List<ShopChangeRequest> getShopChangeRequestPageForAudit(Integer pageNum, Integer pageSize);

    void auditShopChange(Long id, Integer result);
}
