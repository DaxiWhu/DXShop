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
 * 商品 SKU 接口集成测试：选规格展示 / 商家修改展示 / 规格名 / 规格值 / SKU 增改 / 加入购物车。
 * 真实 DB + 真实 Redis + 真实 AuthInterceptor；跨服务 Feign 客户端 mock。
 * 该控制器不显式校验登录态（拦截器对无 token 放行），故以“接口可达”冒烟覆盖。
 * /goods/sku/api 标记 @InternalApi（拦截器直接放行，无需密钥），匿名可访问。
 */
@DisplayName("GoodsSkuController 集成测试")
class GoodsSkuControllerIT extends AbstractIntegrationTest {

    @MockBean
    private UserFeignClient userFeignClient;
    @MockBean
    private OrderFeignClient orderFeignClient;

    @Test
    @DisplayName("选规格展示：接口可达")
    void getSkuChoice_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/goods/sku/1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("商家修改展示：接口可达")
    void getSkuForUserShop_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/goods/sku/shop/1"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("新增规格名：接口可达")
    void addSkuSpecName_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/goods/sku/sku-spec-name/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("新增/更新规格值：接口可达")
    void addAndUpdateSkuSpec_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/goods/sku/sku-spec-value/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("新增/更新 SKU：接口可达")
    void addAndUpdateSku_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/goods/sku/sku/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(handled());
    }

    @Test
    @DisplayName("加入购物车获取 SKU(@InternalApi 匿名可访问)：参数合法时接口可达")
    void getSkuForUserCart_reachable() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/goods/sku/api")
                        .param("skuId", "1").param("buyNum", "1"))
                .andExpect(handled());
    }
}
