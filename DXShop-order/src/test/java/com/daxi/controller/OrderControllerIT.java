package com.daxi.controller;

import com.daxi.feign.UserFeignClient;
import com.daxi.test.AbstractIntegrationTest;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 订单模块集成测试：订单详情（匿名，全链路）、订单状态计数（需登录校验）。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；跨服务 Feign 与 RocketMQ 均 mock（不连 NameServer）。
 */
@DisplayName("OrderController 集成测试")
class OrderControllerIT extends AbstractIntegrationTest {

    @MockBean
    private UserFeignClient userFeignClient;
    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @Test
    @DisplayName("订单详情：不存在的 orderId -> 订单不存在 500")
    void detail_notExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/order/detail/999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("订单状态计数(需登录)：未携带 token -> 未登录 500")
    void categoryNumber_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/order/category-number/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
