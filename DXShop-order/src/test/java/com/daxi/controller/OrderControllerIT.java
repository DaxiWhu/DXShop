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
 * 订单模块集成测试：覆盖订单详情 / 状态计数 / 创建 / 列表 / 地址 / 退款 / 支付 / 评论 等全量端点。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；跨服务 Feign 与 RocketMQ 均 mock。
 * 显式校验登录态的端点（无 token -> 未登录 500）给出确定性断言；其余以“接口可达”冒烟覆盖
 * （含 @InternalApi 的内部接口与 @Valid 校验分支）。
 */
@DisplayName("OrderController 集成测试")
class OrderControllerIT extends AbstractIntegrationTest {

    @MockBean
    private UserFeignClient userFeignClient;
    @MockBean
    private RocketMQTemplate rocketMQTemplate;

    @Test
    @DisplayName("订单详情：不存在的 orderId -> 订单不存在 500")
    void detail_notExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/order/detail/999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("订单状态计数(用户,需登录)：无 token -> 未登录 500")
    void categoryNumber_user_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/order/category-number/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("订单状态计数(用户,需登录)：带 token 接口可达")
    void categoryNumber_user_withAuth() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/order/category-number/user")
                        .header("Authorization", header))
                .andExpect(handled());
    }

    @Test
    @DisplayName("订单状态计数(店铺,需登录)：无 token -> 未登录 500")
    void categoryNumber_shop_withoutAuth() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/order/category-number/shop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("退款详情：不存在的 orderId -> 订单不存在 500")
    void refundRequestDetail_notExist() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/order/refund/requests/999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("内部确认收货(@InternalApi,匿名可访问)：参数合法时接口可达")
    void apiOk_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/order/api/ok")
                        .param("orderId", "1").param("userId", "1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("取消订单：接口可达")
    void cancelOrder_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/order/fail/1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("创建订单(需登录)：接口可达")
    void createOrder_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.post("/order")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("用户订单列表(需登录)：接口可达")
    void simple_user_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/order/simple")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("店铺订单列表(需登录)：接口可达")
    void simple_shop_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/order/simple/shop")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("修改收货地址：接口可达")
    void updateOrderAddress_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/order/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("申请退款：接口可达")
    void sendRefunds_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/order/refunds")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("拉取支付页面：接口可达")
    void payPage_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/order/payments/page").param("orderId", "1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("支付成功回调：接口可达")
    void paymentSuccess_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/order/payments/success")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("确认支付状态：接口可达")
    void checkPaymentStatus_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/order/payments").param("orderId", "1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("用户查看改址申请(需登录)：接口可达")
    void addressRequests_user_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/order/address/requests/user")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("用户查看退款申请(需登录)：接口可达")
    void refundRequests_user_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/order/refund/requests/user")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("店铺查看改址申请(需登录)：接口可达")
    void addressRequests_shop_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/order/address/requests/shop")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("店铺查看退款申请(需登录)：接口可达")
    void refundRequests_shop_reachable() throws Exception {
        String header = authHeader(1L);
        mockMvc.perform(MockMvcRequestBuilders.get("/order/refund/requests/shop")
                        .header("Authorization", header)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("审核改址申请：接口可达")
    void auditAddress_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/order/address/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("审核退款：接口可达")
    void auditRefund_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/order/refund/audit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("物流退货：接口可达")
    void userShip_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/order/refund/user-ship")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("获取发送评论信息：接口可达")
    void commentInfo_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/order/comment")
                        .param("userId", "1").param("orderId", "1")
                        .param("spuId", "1").param("skuId", "1"))
                .andExpect(handled());
    }
}
