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
 * 用户模块集成测试：覆盖注册(匿名) → 登录(匿名) → 查看展示信息(匿名) → 查看隐私信息(需登录) 的端到端链路。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；RedisUtil 被 mock（仅用于绕开验证码发送限制）。
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
}
