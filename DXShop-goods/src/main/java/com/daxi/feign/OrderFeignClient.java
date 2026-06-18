package com.daxi.feign;

import com.daxi.domain.dto.SendCommentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("DXShop-order")
public interface OrderFeignClient {
    @GetMapping("/comment")
    SendCommentDTO getInformationForSendComment(
            @RequestParam Long userId,
            @RequestParam Long orderId,@RequestParam Long spuId,@RequestParam Long skuId);
}
