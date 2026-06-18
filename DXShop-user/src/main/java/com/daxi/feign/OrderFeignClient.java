package com.daxi.feign;


import com.daxi.domain.dto.UserOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "DXShop-order")
public interface OrderFeignClient {
    /**
     * 获取receive订单
     * */
    @PutMapping("/order/api/ok")
    UserOrderDTO getOrderForReceiveOrderAndOkStatus(
            @RequestParam Long orderId,
            @RequestParam Long userId);


}
