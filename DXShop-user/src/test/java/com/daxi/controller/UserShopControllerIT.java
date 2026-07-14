package com.daxi.controller;

import com.daxi.feign.GoodsFeignClient;
import com.daxi.feign.OrderFeignClient;
import com.daxi.test.AbstractIntegrationTest;
import com.daxi.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 店铺模块集成测试：我的店铺 / 登录店铺 / 店铺展示 / 创建 / 审核 / 修改 / 申请 等全量端点。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；RedisUtil 与跨服务 Feign 均 mock。
 * 显式校验登录态（用户/店铺）的端点给出确定性断言；其余以“接口可达”冒烟覆盖。
 */
@DisplayName("UserShopController 集成测试")
class UserShopControllerIT extends AbstractIntegrationTest {

    @MockBean
    private RedisUtil redisUtil;
    @MockBean
    private GoodsFeignClient goodsFeignClient;
    @MockBean
    private OrderFeignClient orderFeignClient;

    @Test
    @DisplayName("我的店铺(需登录)：无 token -> 未登录 500")
    void getUserShop_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user-shop/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("我的店铺(需登录)：带 token 接口可达")
    void getUserShop_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/user-shop/my")
                        .header("Authorization", header))
                .andExpect(handled());
    }

    @Test
    @DisplayName("登录店铺(需登录)：无 token -> 未登录 500")
    void loginShop_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user-shop/login/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("登录店铺(需登录)：带 token 接口可达")
    void loginShop_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.post("/user-shop/login/1")
                        .header("Authorization", header))
                .andExpect(handled());
    }

    @Test
    @DisplayName("店铺展示(匿名)：不存在的 shopId -> 店铺不存在 500")
    void getUserShopShow_notExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user-shop/show/999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("创建店铺(需登录)：无 token -> 未登录 500")
    void createShop_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user-shop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("创建店铺(需登录)：带 token 接口可达")
    void createShop_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.post("/user-shop")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("审核创建店铺申请：接口可达")
    void auditShop_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user-shop/audit/1/1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("审核查看创建店铺申请：接口可达")
    void getShopAudit_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user-shop/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("店铺修改申请(需店铺登录)：无 token -> 未登录 500")
    void getShopChange_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user-shop/request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("店铺修改申请(需店铺登录)：带 token 接口可达")
    void getShopChange_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/user-shop/request")
                        .header("Authorization", header))
                .andExpect(handled());
    }

    @Test
    @DisplayName("审核员查看修改申请：接口可达")
    void getShopChangeAudit_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user-shop/request/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("审核员审核修改申请：接口可达")
    void auditShopChange_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user-shop/request/audit/1/1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("修改店铺信息(需店铺登录)：无 token -> 未登录 500")
    void updateShop_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user-shop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("修改店铺信息(需店铺登录)：带 token 接口可达")
    void updateShop_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.put("/user-shop")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("店铺全部信息(需店铺登录)：无 token -> 未登录 500")
    void getUserShopAll_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user-shop/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("店铺全部信息(需店铺登录)：带 token 接口可达")
    void getUserShopAll_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/user-shop/all")
                        .header("Authorization", header))
                .andExpect(handled());
    }
}
