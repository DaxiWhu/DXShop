package com.daxi.controller;

import com.daxi.feign.GoodsFeignClient;
import com.daxi.feign.OrderFeignClient;
import com.daxi.test.AbstractIntegrationTest;
import com.daxi.util.RedisUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户模块集成测试：覆盖注册(匿名) → 登录(匿名) → 展示/隐私信息 → 收藏/购物车/收货/评论 等全量端点。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；RedisUtil 被 mock（仅用于绕开验证码发送限制），
 * 跨服务的 Feign 客户端 mock 掉（测试不依赖未启动的 order/goods 服务）。
 */
@DisplayName("UserController 集成测试")
class UserControllerIT extends AbstractIntegrationTest {

    @MockBean
    private RedisUtil redisUtil;
    @MockBean
    private GoodsFeignClient goodsFeignClient;
    @MockBean
    private OrderFeignClient orderFeignClient;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private void seedUser(String phone, String password) throws Exception {
        when(redisUtil.isVerifyCodeLimited(anyString())).thenReturn(false);
        when(redisUtil.getVerifyCode(phone)).thenReturn("123456");
        mockMvc.perform(MockMvcRequestBuilders.post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("phone", phone, "password", password, "verifyCode", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private Long loginAndGetUserId(String phone, String password) throws Exception {
        String body = mockMvc.perform(MockMvcRequestBuilders.post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("phone", phone, "password", password))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").exists())
                .andExpect(jsonPath("$.data.token").value(org.hamcrest.Matchers.startsWith("Bearer ")))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("data").get("userId").asLong();
    }

    @Test
    @DisplayName("注册(匿名)：验证码正确 -> 写入DB返回成功")
    void register_success() throws Exception {
        seedUser("13812345678", "password123");
    }

    @Test
    @DisplayName("注册(匿名)：验证码错误 -> 业务异常 500")
    void register_wrongCode_fails() throws Exception {
        when(redisUtil.isVerifyCodeLimited(anyString())).thenReturn(false);
        when(redisUtil.getVerifyCode("13812345678")).thenReturn("999999");

        mockMvc.perform(MockMvcRequestBuilders.post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("phone", "13812345678", "password", "password123", "verifyCode", "123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("登录(匿名)：正确凭证 -> 返回带 Bearer 前缀的 token 与 userId")
    void login_success() throws Exception {
        seedUser("13812345678", "password123");
        loginAndGetUserId("13812345678", "password123");
    }

    @Test
    @DisplayName("查看展示信息(匿名)：注册后昵称为 '用户'+手机号后4位")
    void getDisplayInfo_anonymous() throws Exception {
        seedUser("13812345678", "password123");
        Long userId = loginAndGetUserId("13812345678", "password123");

        mockMvc.perform(MockMvcRequestBuilders.get("/user/display/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("用户5678"));
    }

    @Test
    @DisplayName("查看隐私信息(需登录)：带合法 token -> 手机号脱敏返回")
    void getPrivateInfo_withAuth() throws Exception {
        seedUser("13812345678", "password123");
        Long userId = loginAndGetUserId("13812345678", "password123");
        String header = authHeader(userId);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/private")
                        .header("Authorization", header))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.phone").value("138****5678"));
    }

    @Test
    @DisplayName("查看隐私信息(需登录)：无 token -> 未登录 500")
    void getPrivateInfo_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/private"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("获取验证码(匿名)：合法手机号接口可达")
    void getVerifyCode_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/verify-code").param("phone", "13812345678"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("收藏店铺列表(需登录)：无 token -> 未登录 500")
    void getFavoritesShop_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/favorites/shop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("收藏店铺列表(需登录)：带 token 接口可达")
    void getFavoritesShop_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/user/favorites/shop")
                        .header("Authorization", header))
                .andExpect(handled());
    }

    @Test
    @DisplayName("收藏商品列表(需登录)：无 token -> 未登录 500")
    void getFavoritesSpu_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/favorites/spu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("我的购物车(需登录)：无 token -> 未登录 500")
    void getMyCart_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("我的购物车(需登录)：带 token 接口可达")
    void getMyCart_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/user/cart")
                        .header("Authorization", header))
                .andExpect(handled());
    }

    @Test
    @DisplayName("收藏店铺(需登录)：无 token -> 未登录 500")
    void followShop_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/favorites/shop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("收藏商品(需登录)：无 token 时返回未关注(成功 200)")
    void followGoods_noToken_returnsNotFollow() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user/favorites/spu/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("加入购物车(需登录)：接口可达")
    void addCart_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.put("/user/cart")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("购物车勾选(需登录)：接口可达")
    void cartChecked_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.put("/user/cart/checked")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("购物车删除(需登录)：接口可达")
    void deleteCart_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/user/cart")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("确认收货：接口可达")
    void receiveOrder_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/user/order/ok/1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("获取评论信息(@InternalApi 匿名可访问)：参数合法时接口可达")
    void commentInformation_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/comment/information")
                        .param("userId", "1").param("shopId", "1")
                        .param("orderId", "1").param("spuId", "1").param("skuId", "1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("修改展示信息(需登录)：接口可达")
    void updateDisplay_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.put("/user/display")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("修改隐私信息(需登录)：接口可达")
    void updatePrivate_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.put("/user/private")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("进入店铺是否关注(匿名)：无 token 返回未关注(成功 200)")
    void checkIsFollowShop_noToken_returnsNotFollow() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/favorites/shop/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("进入商品是否关注(匿名)：无 token 返回未关注(成功 200)")
    void checkIsFollowSpu_noToken_returnsNotFollow() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/favorites/spu/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
