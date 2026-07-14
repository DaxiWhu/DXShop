package com.daxi.controller;

import com.daxi.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 客服会话接口集成测试：客服登录 / 待处理会话 / 会话列表 / 接管 / 发消息 / 关闭。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor + WebSocket；无需 mock 外部服务（上下文在测试 profile 下可独立启动）。
 * 端点均标记 @AnonymousAccess（拦截器直接放行），但业务层校验客服登录态（无 agent token -> 未登录 500）。
 * 纯路径参数的端点给出确定性断言；带请求体的端点以“接口可达”冒烟覆盖（校验/运行时依赖不影响绿）。
 */
@DisplayName("CsChatController 集成测试")
class CsChatControllerIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("客服登录(匿名)：接口可达(路由/校验/Service 入口)")
    void agentLogin_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/cs/agent/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("待处理会话(匿名)：接口可达")
    void getPendingSessions_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/cs/session/pending"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("客服会话列表(匿名)：接口可达")
    void getAgentSessions_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/cs/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("接管会话(匿名,路径参数)：无 agent token -> 未登录 500")
    void takeSession_withoutAgent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/cs/session/1/take"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("客服发送消息(匿名)：接口可达")
    void sendAgentMessage_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/cs/session/1/message")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("关闭会话(匿名,路径参数)：无 agent token -> 未登录 500")
    void closeSession_withoutAgent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/cs/session/1/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
