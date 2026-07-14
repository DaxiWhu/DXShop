package com.daxi.controller;

import com.daxi.feign.GoodsFeignClient;
import com.daxi.feign.OrderFeignClient;
import com.daxi.test.AbstractIntegrationTest;
import com.daxi.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 静态数据接口集成测试：订单静态数据失效。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；RedisUtil 与跨服务 Feign 均 mock。
 * 该接口不校验登录态（拦截器对无 token 放行），以“接口可达”冒烟覆盖。
 */
@DisplayName("StaticController 集成测试")
class StaticControllerIT extends AbstractIntegrationTest {

    @MockBean
    private RedisUtil redisUtil;
    @MockBean
    private GoodsFeignClient goodsFeignClient;
    @MockBean
    private OrderFeignClient orderFeignClient;

    @Test
    @DisplayName("订单静态数据失效：接口可达(路由/Service 入口)")
    void invalidateOrderData_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/order-data/1"))
                .andExpect(handled());
    }
}
