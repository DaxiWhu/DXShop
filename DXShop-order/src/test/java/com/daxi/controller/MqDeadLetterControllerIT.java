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
 * MQ 死信消息管理接口集成测试：分页查询 / 未处理数量 / 详情 / 处理 / 忽略 / 批量删除。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；跨服务 Feign 与 RocketMQ 均 mock。
 * 该控制器直接操作 MqDeadLetterMessageMapper（真实 DB），不经登录校验，故以数据不存在分支 + 接口可达覆盖。
 */
@DisplayName("MqDeadLetterController 集成测试")
class MqDeadLetterControllerIT extends AbstractIntegrationTest {

    @MockBean
    private UserFeignClient userFeignClient;
    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @Test
    @DisplayName("分页查询：空表返回空页 -> 成功 200")
    void page_empty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/mq/dead-letter/page"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未处理数量：接口可达")
    void unprocessedCount_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/mq/dead-letter/unprocessed-count"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("详情：不存在的 id -> 记录不存在 500")
    void getById_notExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/mq/dead-letter/999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("标记为已处理：不存在的 id -> 记录不存在 500")
    void handle_notExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/mq/dead-letter/handle/999999999")
                        .param("handler", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("忽略：不存在的 id -> 记录不存在 500")
    void ignore_notExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/mq/dead-letter/ignore/999999999")
                        .param("handler", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("批量删除：空 ID 列表 -> 参数错误 500")
    void batchDelete_empty() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/mq/dead-letter/batch-delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
