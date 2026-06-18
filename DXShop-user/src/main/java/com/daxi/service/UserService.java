package com.daxi.service;

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

import java.util.List;

public interface UserService {

    /**
     * 获取验证码
     */
    String getVerifyCode(String phone);

    /**
     * 用户注册
     */
    void register(RegisterRequestAO registerRequest);

    /**
     * 用户登录
     */
    LoginResponseDTO login(LoginRequestAO loginRequest);
    void followShop(Long shopId,Long userId);

    void followGoods(Long spuId, Long userId);

    void addSkuToShoppingList(UserFollowSkuAO ao, Long userId);

    void changeCheckedSkuInShoppingList(Long userId, List<Long> cartIds);

    List<UserCart> getMyCart(Long userId);

    List<UserFavoritesShopDTO> getMyFavoritesShops(Long userId);

    List<UserFavoritesSpuDTO> getMyFavoritesSpus(Long userId);

    void receiveOrder(Long orderId);

    void deleteSkuInShoppingList(Long userId,List<Long> cartIds);

    Integer checkIsFollowShop(Long userId, Long shopId);

    Integer checkIsFollowSpu(Long userId, Long spuId);

    SendCommentDTO getInformationForSendComment(Long userId,Long shopId, Long orderId, Long spuId, Long skuId);

    /**
     * 修改展示信息
     */
    void updateDisplayInfo(UserDisplayAO ao, Long userId);

    /**
     * 修改隐私信息
     */
    void updatePrivateInfo(UserPrivateAO ao, Long userId);

    /**
     * 获取展示信息
     */
    UserDisplayDTO getDisplayInfo(Long userId);

    /**
     * 获取隐私信息
     */
    UserPrivateDTO getPrivateInfo(Long userId);
}
