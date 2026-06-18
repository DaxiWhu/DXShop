package com.daxi.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "DXShop-user")
public interface UserFeignClient {
    @DeleteMapping("/order-data/{orderId}")
    void invalidateOrderData(@PathVariable Long orderId);
}
