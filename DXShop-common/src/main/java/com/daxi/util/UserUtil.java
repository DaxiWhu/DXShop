package com.daxi.util;

import org.springframework.stereotype.Component;

@Component
public class UserUtil {
    private static final ThreadLocal<LoginContext> USER_THREAD_LOCAL =
            ThreadLocal.withInitial(LoginContext::new);
    public static void setLocalUserId(Long userId) {
        USER_THREAD_LOCAL.get().setUserId(userId);
    }
    public static void setLocalShopId(Long shopId) {
        USER_THREAD_LOCAL.get().setShopId(shopId);
    }
    public static void setAgentId(Long agentId) {
        USER_THREAD_LOCAL.get().setAgentId(agentId);
    }
    public static Long getLocalUserId(){
        return USER_THREAD_LOCAL.get().getUserId();
    }
    public static Long getAgentId(){
        return USER_THREAD_LOCAL.get().getAgentId();
    }
    public static Long getLocalShopId(){
        return USER_THREAD_LOCAL.get().getShopId();
    }
    public static Long getOrderId(){
        return USER_THREAD_LOCAL.get().getUserId();
    }
    /**
     * 清除当前线程的用户信息（非常重要，必须在拦截器后置处理中调用）
     */
    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }



}
