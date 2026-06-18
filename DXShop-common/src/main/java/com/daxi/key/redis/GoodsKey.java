package com.daxi.key.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GoodsKey {

    // --- 商品详情缓存 ---
    SKU_DETAIL_CACHE("sku:info:%d", "SKU详细信息缓存"),
    SPU_DETAIL_CACHE("spu:info:%d", "SPU详细信息缓存"),

    // --- 商品库存 ---
    GOODS_STOCK("goods:stock:%d", "商品库存扣减Key"),

    // --- 其他常用 Key (可以根据需要继续添加) ---
    GOODS_CATEGORY_LIST("goods:category:list", "商品分类列表缓存");

    private final String template;
    private final String desc;

    /**
     * 核心方法：根据模板生成 Key
     * 例如：GoodsKey.SKU_DETAIL_CACHE.format(1001)
     * 返回："sku:info:1001"
     */
    public String format(Object... args) {
        return String.format(this.template, args);
    }
}
