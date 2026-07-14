package com.daxi.controller;

import com.daxi.feign.UserFeignClient;
import com.daxi.test.AbstractIntegrationTest;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 地址模块集成测试：新增/修改、删除、查看地址。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；跨服务 Feign 与 RocketMQ 均 mock。
 * 删除/查看地址显式校验登录态（无 token -> 未登录 500），新增修改不校验（拦截器对无 token 放行）。
 */
@DisplayName("AddressController 集成测试")
class AddressControllerIT extends AbstractIntegrationTest {

    @MockBean
    private UserFeignClient userFeignClient;
    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @Test
    @DisplayName("查看地址(需登录)：无 token -> 未登录 500")
    void getAddresses_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("查看地址(需登录)：带 token 接口可达")
    void getAddresses_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/address")
                        .header("Authorization", header))
                .andExpect(handled());
    }

    @Test
    @DisplayName("删除地址(需登录)：无 token -> 未登录 500")
    void deleteAddress_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/address/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("删除地址(需登录)：带 token 接口可达")
    void deleteAddress_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/address/1")
                        .header("Authorization", header))
                .andExpect(handled());
    }

    @Test
    @DisplayName("新增/修改地址：接口可达(路由/校验/Service 入口)")
    void changeAddress_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }
}
