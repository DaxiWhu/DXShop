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
 * OSS 接口集成测试：上传策略获取。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；RedisUtil 与跨服务 Feign 均 mock。
 * 该接口不校验登录态（拦截器对无 token 放行），以“接口可达”冒烟覆盖（OSS 密钥由测试 profile 占位符提供）。
 */
@DisplayName("OssController 集成测试")
class OssControllerIT extends AbstractIntegrationTest {

    @MockBean
    private RedisUtil redisUtil;
    @MockBean
    private GoodsFeignClient goodsFeignClient;
    @MockBean
    private OrderFeignClient orderFeignClient;

    @Test
    @DisplayName("上传策略获取：接口可达(路由/OSS 客户端/Service 入口)")
    void upload_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/oss/upload").param("key", "test/avatar.png"))
                .andExpect(handled());
    }
}
