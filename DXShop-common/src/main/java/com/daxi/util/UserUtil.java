package com.daxi.util;

import org.springframework.stereotype.Component;

@Component
public class UserUtil {
    private static final ThreadLocal<Long> USER_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Long> SHOP_THREAD_LOCAL = new ThreadLocal<>();
    public static void setLocalUserId(Long userId) {
        USER_THREAD_LOCAL.set(userId);
    }
    public static Long getLocalUserId(){
        return USER_THREAD_LOCAL.get();
    }

    public static Long getLocalShopId(){
        return SHOP_THREAD_LOCAL.get();
    }
    public static Long getOrderId(){
        return USER_THREAD_LOCAL.get();
    }
    /**
     * 清除当前线程的用户信息（非常重要，必须在拦截器后置处理中调用）
     */
    public static void remove() {
        USER_THREAD_LOCAL.remove();
        SHOP_THREAD_LOCAL.remove();
    }


    public static void setLocalShopId(Long shopId) {
        SHOP_THREAD_LOCAL.set(shopId);
    }
}
