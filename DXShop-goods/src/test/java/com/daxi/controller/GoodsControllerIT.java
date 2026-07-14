package com.daxi.controller;

import com.daxi.feign.OrderFeignClient;
import com.daxi.feign.UserFeignClient;
import com.daxi.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 商品模块集成测试：商品详情 / 简要列表（匿名接口）+ 商家商品增改（需登录）。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；跨服务 Feign 客户端 mock。
 * 表初始为空，故匿名读取主要验证“不存在/空结果”分支（走完 controller→service→mapper→DB 全链路）。
 * 商家端点不显式校验登录态（拦截器对无 token 放行），故以“接口可达”冒烟覆盖。
 */
@DisplayName("GoodsController 集成测试")
class GoodsControllerIT extends AbstractIntegrationTest {

    @MockBean
    private UserFeignClient userFeignClient;
    @MockBean
    private OrderFeignClient orderFeignClient;

    @Test
    @DisplayName("商品详情(匿名)：不存在的 spuId -> 商品不存在 500")
    void detail_notExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/goods/detail/999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("商品简要(匿名)：空结果 -> SERVER_BUSY 500")
    void simple_empty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/goods/simple").param("ids", "999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("商家商品分页(需登录)：接口可达(路由/Service 入口)")
    void shopSpu_get() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/goods/shop/spu")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("修改商品(需登录)：接口可达")
    void shopSpu_update() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.put("/goods/shop/spu/1")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("新增商品(需登录)：接口可达")
    void shopSpu_add() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.post("/goods/shop/spu")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }
}
