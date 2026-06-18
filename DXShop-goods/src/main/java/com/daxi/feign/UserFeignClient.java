package com.daxi.feign;

import com.daxi.domain.dto.SendCommentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "DXShop-user")
public interface UserFeignClient {

    @GetMapping("/user/comment/information")
    SendCommentDTO getInformationForSendComment(
            @RequestParam("userId") Long userId,
            @RequestParam("shopId") Long shopId,
            @RequestParam("orderId") Long orderId,
            @RequestParam("spuId") Long spuId,
            @RequestParam("skuId") Long skuId);


    @GetMapping("/user/shop/name")
    String getShopName(@RequestParam("shopId") Long shopId);
}
