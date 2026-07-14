package com.daxi;

import com.daxi.service.ChatService;
import com.daxi.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Chat 模块应用上下文冒烟测试：仅验证完整 Spring 上下文（含真实 Redis 连接、
 * WebSocket、MyBatis）能在测试 profile 下启动。后端由 TestProfileResolver 决定
 * （本地共享机 / CI 容器）。
 *
 * 命名为 *IT，由 Failsafe 在 verify 阶段执行；mvn test 不会触发（保证纯单测无需中间件）。
 */
@DisplayName("Chat 模块应用上下文冒烟测试")
class DxShopChatApplicationIT extends AbstractIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Test
    void contextLoads() {
        assertThat(chatService).isNotNull();
    }
}
