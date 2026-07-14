package com.daxi.controller;

import com.daxi.feign.OrderFeignClient;
import com.daxi.feign.UserFeignClient;
import com.daxi.test.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * 商品评论接口集成测试：获取评论 / 发送评论。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；跨服务 Feign 客户端 mock。
 * 该控制器不显式校验登录态（拦截器对无 token 放行），故以“接口可达”冒烟覆盖全链路。
 */
@DisplayName("GoodsCommentController 集成测试")
class GoodsCommentControllerIT extends AbstractIntegrationTest {

    @MockBean
    private UserFeignClient userFeignClient;
    @MockBean
    private OrderFeignClient orderFeignClient;

    @Test
    @DisplayName("获取评论：接口可达(路由/拦截器/Service 入口)")
    void getComment_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/goods/comment/1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("发送评论：接口可达(路由/校验/Service 入口)")
    void sendComment_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/goods/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }
}
