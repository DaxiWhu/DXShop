package com.daxi.feign;


import com.daxi.domain.dto.UserCartSkuDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "DXShop-goods")
public interface GoodsFeignClient {
    /**
     * 为了加入购物车获取sku信息
     * */
    @GetMapping("/goods/sku/api")
    UserCartSkuDTO getSkuForUserCartById(
            @RequestParam Long skuId, @RequestParam Integer buyNum);

}
