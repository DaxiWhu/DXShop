package com.daxi.key.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderKey {
    //以下是redis在order相关的key
    // --- 锁相关 ---
    ORDER_SUBMIT_LOCK("order:submit:lock:%d", "下单防重提交锁"),
    ORDER_PAY_LOCK("order:pay:lock:%d", "订单支付锁"),
    // --- 库存相关 ---
    GOODS_STOCK("goods:stock:%d", "商品库存扣减Key"),

    // --- 订单索引 (ZSet) ---
    ORDER_USER_ALL("order:user:%d:all", "用户所有订单索引"),
    ORDER_USER_STATUS("order:user:%d:status:%d", "用户指定状态订单索引"),
    ORDER_SHOP_STATUS("order:shop:%d:status:%d", "店铺指定状态订单索引"),

    // --- 订单详情缓存 ---
    ORDER_DETAIL("order:%d", "订单详情Hash缓存"),

    // --- 业务队列/申请 ---
    ORDER_CHANGE_ADDRESS_USER_REQUEST("order:address:user:%d:%d", "用户申请更换订单地址ZSet()"),
    ORDER_CHANGE_ADDRESS_SHOP_REQUEST("order:address:shop:%d:%d", "店铺查看用户申请更换订单地址ZSet()"),
    ORDER_CHANGE_ADDRESS_DETAIL("order:address:%d", "订单更换地址详情Hash缓存"),
    REFUND_DETAIL("refund:%d", "退款详情Hash缓存"),
    REFUND_SHOP_LIST("refund:shop:%d:%d", "店铺退款审核ZSet(shopId:status)"),
    REFUND_USER_LIST("refund:user:%d:%d", "用户退款记录ZSet(userId:status)");

    private final String template;
    private final String desc;

    /**
     * 核心方法：根据模板生成 Key
     * 例如：ORDER_CHANGE_ADDRESS_REQUEST.format(1001)
     * 返回："order:1001:address:request"
     */
    public String format(Object... args) {
        return String.format(this.template, args);
    }



}
