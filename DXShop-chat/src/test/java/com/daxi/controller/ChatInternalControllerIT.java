package com.daxi.controller;

import com.daxi.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户会话接口集成测试：创建会话 / 会话列表 / 会话详情 / 发消息 / 关闭。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor + WebSocket；无需 mock 外部服务。
 * 端点显式校验用户登录态（无 token -> 未登录 500），纯路径参数的端点给出确定性断言；
 * 带请求体的端点以“接口可达”冒烟覆盖。
 */
@DisplayName("ChatInternalController 集成测试")
class ChatInternalControllerIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("创建会话(需登录)：无 token -> 未登录 500")
    void createSession_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/chat/session").param("spuId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("创建会话(需登录)：带 token 接口可达")
    void createSession_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.post("/chat/session")
                        .header("Authorization", header).param("spuId", "1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("获取会话列表(需登录)：无 token -> 未登录 500")
    void getSessions_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/chat/session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("会话详情(需登录,路径参数)：无 token -> 未登录 500")
    void getSessionDetail_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/chat/session/1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("用户发送消息(需登录)：带 token 接口可达")
    void sendUserMessage_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.post("/chat/session/1/message/user")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("关闭会话(需登录,路径参数)：无 token -> 未登录 500")
    void closeSession_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/chat/session/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
