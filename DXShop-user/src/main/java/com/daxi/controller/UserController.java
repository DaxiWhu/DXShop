package com.daxi.controller;


import com.daxi.annotation.AnonymousAccess;
import com.daxi.annotation.InternalApi;
import com.daxi.domain.ao.LoginRequestAO;
import com.daxi.domain.ao.RegisterRequestAO;
import com.daxi.domain.ao.UserDisplayAO;
import com.daxi.domain.ao.UserFollowSkuAO;
import com.daxi.domain.ao.UserPrivateAO;
import com.daxi.domain.dto.LoginResponseDTO;
import com.daxi.domain.dto.SendCommentDTO;
import com.daxi.domain.dto.UserDisplayDTO;
import com.daxi.domain.dto.UserFavoritesShopDTO;
import com.daxi.domain.dto.UserFavoritesSpuDTO;
import com.daxi.domain.dto.UserPrivateDTO;
import com.daxi.domain.entity.UserCart;
import com.daxi.result.Result;
import com.daxi.service.UserService;
import com.daxi.util.UserUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.daxi.limit.UserLimit.USER_NOT_FOLLOW;
import static com.daxi.response.CommonResponse.ID_ERROR;
import static com.daxi.response.CommonResponse.PARAM_EMPTY;
import static com.daxi.response.OrderResponse.PARAM_ERROR;
import static com.daxi.response.UserResponse.NOT_LOGIN;
import static com.daxi.response.UserResponse.VERIFY_CODE_SEND_SUCCESS;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final @NonNull UserService userService;

    public static final int MIN_ID_SCALE = 1;

    /**获取验证码1*/
    @GetMapping("/verify-code")
    @AnonymousAccess
    public Result<String> getVerifyCode(
            @RequestParam
            @NotBlank(message = PARAM_ERROR)
            @Pattern(regexp = "^1[3-9]\\d{9}$", message = PARAM_ERROR)
            String phone) {
        String code = userService.getVerifyCode(phone);
        return Result.success(VERIFY_CODE_SEND_SUCCESS, code);
    }
    /**根据验证码注册1*/

    @PostMapping("/register")
    @AnonymousAccess
    public Result<Void> register(
            @Valid @RequestBody RegisterRequestAO registerRequest) {
        userService.register(registerRequest);
        return Result.success();
    }
    /**根据手机号登录1*/
    @PostMapping("/login")
    @AnonymousAccess
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestAO loginRequest) {
        LoginResponseDTO response = userService.login(loginRequest);
        return Result.success(response);
    }


    /**关注别人或者取消收藏
    @PutMapping("/favorites/user/{shopId}")
    public Result<Void> followUser(@PathVariable Long UserId){
        userService.followUser(UserId);
        return Result.success();
    }
     */
    /**收藏店铺或者取消收藏1*/
    @PutMapping("/favorites/shop/{shopId}")
    public Result<Void> followShop(
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long shopId){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        userService.followShop(shopId, userId);
        return Result.success();
    }
    /**收藏商品或者取消收藏1*/
    @PutMapping("/favorites/spu/{spuId}")
    public Result<Void> followGoods(
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long spuId){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        userService.followGoods(spuId,userId);
        return Result.success();
    }
    /**加入购物车1*/
    @PutMapping("/cart")
    public Result<Void> addSkuToShoppingList(@Valid @RequestBody UserFollowSkuAO ao){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        userService.addSkuToShoppingList(ao, userId);
        return Result.success();
    }
    /**购物车勾选1*/
    @PutMapping("/cart/checked")
    public Result<Void> checkedSkuInShoppingList(
            @Valid
            @RequestBody
            List<@Min(value = MIN_ID_SCALE, message = ID_ERROR) Long> cartIds){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        userService.changeCheckedSkuInShoppingList(userId, cartIds);
        return Result.success();
    }

    /**购物车删除1*/
    @DeleteMapping("/cart")
    public Result<Void> deleteSkuInShoppingList(
            @Valid
            @RequestBody
            List<@Min(value = MIN_ID_SCALE, message = ID_ERROR) Long> cartIds){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        userService.deleteSkuInShoppingList(userId, cartIds);
        return Result.success();
    }





    //到这里11


    /**发送确定收货，然后正式加入其他的关联表里面10*/
    @PostMapping("/order/ok/{orderId}")
    public Result<Void> receiveOrder(
            @PathVariable
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            Long orderId){
        userService.receiveOrder(orderId);
        return Result.success();
    }

    /**申请客服对话（目前不实现）
    @PostMapping("/order/service")
    public Result<Void> sendService(@RequestBody UserOrderServiceAO service){
        userService.sendService(service);
        return Result.success();
    }*/
    /**更改默认地址*/



    /**查看自己的收藏店铺1*/
    @GetMapping("/favorites/shop")
    public Result<List<UserFavoritesShopDTO>> getMyFavoritesShops(){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }

        return Result.success(userService.getMyFavoritesShops(userId));
    }
    /**进入店铺时刷新是否关注1*/
    @GetMapping("/favorites/shop/{shopId}")
    public Result<Integer> checkIsFollowShop(
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long shopId){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.success(USER_NOT_FOLLOW);
        }
        return Result.success(userService.checkIsFollowShop(userId,shopId));
    }
    /**进入商品时刷新是否关注1*/
    @GetMapping("/favorites/spu/{spuId}")
    public Result<Integer> checkIsFollowSpu(
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long spuId){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.success(USER_NOT_FOLLOW);
        }
        return Result.success(userService.checkIsFollowSpu(userId, spuId));
    }

    /**查看自己的收藏商品1*/
    @GetMapping("/favorites/spu")
    public Result<List<UserFavoritesSpuDTO>> getMyFavoritesSpus(){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(userService.getMyFavoritesSpus(userId));
    }
    /**查看自己的收藏用户
    @GetMapping("/favorites/user")
    public Result<List<UserFavoritesUserDTO>> getMyFavoritesUsers(){
        return userService.getMyFavoritesUsers();
    }
   */



    /**查看自己的购物车1*/
    @GetMapping("/cart")
    public Result<List<UserCart>> getMyCart(){
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(userService.getMyCart(userId));
    }

    /**获取发表评论所需信息（内部接口）10*/
    @GetMapping("/comment/information")
    @InternalApi
    public SendCommentDTO getInformationForSendComment(
            @RequestParam Long userId,
            @RequestParam Long shopId,
            @RequestParam Long orderId,
            @RequestParam Long spuId,
            @RequestParam Long skuId){
        return userService.getInformationForSendComment(userId,shopId, orderId, spuId, skuId);
    }

    /**查看自己的粉丝
    @GetMapping("/fans")
    public Result<List<UserFansDTO>> getMyFans(){
        return userService.getMyFans();
    }
    */
    /**修改展示信息1*/
    @PutMapping("/display")
    public Result<Void> updateDisplayInfo(
            @NotNull
            @Valid @RequestBody UserDisplayAO ao) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            return Result.fail(NOT_LOGIN);
        }
        userService.updateDisplayInfo(ao, userId);
        return Result.success();
    }

    /**修改隐私信息*/
    @PutMapping("/private")
    public Result<Void> updatePrivateInfo(
            @NotNull
            @Valid @RequestBody UserPrivateAO ao) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            return Result.fail(NOT_LOGIN);
        }
        userService.updatePrivateInfo(ao, userId);
        return Result.success();
    }

    /**
     * 获取展示信息
     */
    @GetMapping("/display/{userId}")
    @AnonymousAccess
    public Result<UserDisplayDTO> getDisplayInfo(
         @Valid @Min(MIN_ID_SCALE)  @PathVariable Long userId) {
        return Result.success(userService.getDisplayInfo(userId));
    }

    /**获取隐私信息*/
    @GetMapping("/private")
    public Result<UserPrivateDTO> getPrivateInfo() {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(userService.getPrivateInfo(userId));
    }
}
