package com.daxi.controller;


import com.daxi.domain.ao.GetShopChangeRequestAO;
import com.daxi.domain.ao.GetShopCreateRequestPageAO;
import com.daxi.domain.ao.UserShopAO;
import com.daxi.domain.ao.UserShopCreateAO;
import com.daxi.domain.dto.LoginResponseDTO;
import com.daxi.domain.dto.UserShopDTO;
import com.daxi.domain.dto.UserShopSimpleDTO;
import com.daxi.domain.entity.ShopChangeRequest;
import com.daxi.domain.entity.UserShopDisplay;
import com.daxi.result.Result;
import com.daxi.service.UserShopService;
import com.daxi.util.UserUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.daxi.response.CommonResponse.ID_ERROR;
import static com.daxi.response.UserResponse.NOT_LOGIN;
import static com.daxi.response.UserShopResponse.SHOP_NOT_EXIST;


@RestController
@RequestMapping("/user-shop")
@RequiredArgsConstructor
public class UserShopController {
    private final @NonNull UserShopService userShopService;

    public static final int MIN_ID_SCALE=1;
    /**查看我拥有的店铺1*/
    @GetMapping("/my")
    public Result<List<UserShopSimpleDTO>> getUserShop() {
        Long userId = UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(userShopService.getMyShopByuserId(userId));
    }
    /**登录我的店铺1*/
    @PostMapping("/login/{shopId}")
    public Result<LoginResponseDTO> loginShop(
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long shopId) {
        Long userId=UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }

        return Result.success(userShopService.loginShop(shopId,userId));
    }

    /**获取店铺提供展示的字段1*/
    @GetMapping("/show/{shopId}")
    public Result<UserShopDisplay> getUserShopShow(
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long shopId) {
        UserShopDisplay shopShowById = userShopService.getShopShowById(shopId);
        if(shopShowById== null){
            return Result.fail(SHOP_NOT_EXIST);
        }
        return Result.success(shopShowById);
    }

    /**商家创建店铺1*/
    @PostMapping("")
    public Result<Void> createShop(@Valid @RequestBody UserShopCreateAO ao){
        Long userId = UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        userShopService.createShop(ao,userId);
        return Result.success();
    }
    /**审核创建店铺申请1*/
    @PutMapping("/audit/{shopId}/{result}")
    public Result<Void> auditShop(
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long shopId,
            @PathVariable Integer result) {
        userShopService.auditShop(shopId, result);
        return Result.success();
    }
    /**审核查看创建店铺申请1*/
    @GetMapping("/audit")
    public Result<List<UserShopDTO>> getShopAudit(@Valid @RequestBody GetShopCreateRequestPageAO ao) {
        return Result.success(userShopService.getShopAudit(ao));
    }
    /**店铺主查看修改表里面的申请1*/
    @GetMapping("/request")
    public Result<List<ShopChangeRequest>> getShopChange() {
        Long shopId = UserUtil.getLocalShopId();
        if(shopId==null){
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(userShopService.getShopChangeRequest(shopId));
    }
    /**审核员查看修改表里面的申请1*/
    @GetMapping("/request/audit")
    public Result<List<ShopChangeRequest>> getShopChange(
            @Valid @RequestBody GetShopChangeRequestAO ao) {
        return Result.success(userShopService.getShopChangeRequestPageForAudit(ao.getPageNum(),ao.getPageSize()));

    }
    /**审核员审核修改表里面的申请1*/
    @PutMapping("/request/audit/{id}/{result}")
    public Result<Void> auditShopChange(
            @PathVariable
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            Long id,
            @PathVariable Integer result) {
        userShopService.auditShopChange(id,result);
        return Result.success();
    }
    /**
     * 修改店铺信息1
     * */
    @PutMapping("")
    public Result<String> updataShop(
            @Valid
            @RequestBody UserShopAO ao) {
        Long shopId = UserUtil.getLocalShopId();
        if(shopId==null){
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(userShopService.updateShop(shopId, ao));
    }

    /**
     * 商家修改自身信息时调用1
     *
     *1
     */
    @GetMapping("/all")
    public Result<UserShopDTO> getUserShopAll() {
        Long shopId = UserUtil.getLocalShopId();
        if(shopId==null){
            return Result.fail(NOT_LOGIN);
        }
        UserShopDTO shopAllById = userShopService.getShopAllById(shopId);
        if(shopAllById==null){
            return Result.fail(SHOP_NOT_EXIST);
        }
        return Result.success(shopAllById);
    }

}
