package com.daxi.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserUtil 单元测试。
 *
 * ThreadLocal 采用 ThreadLocal.withInitial(LoginContext::new) 初始化，
 * 因此未显式 set 的线程上调用 getLocalUserId() 会安全地返回 null（而不是抛 NPE）。
 * remove() 之后再次读取同样回到「返回 null」的安全状态。
 */
@DisplayName("UserUtil 单元测试")
class UserUtilTest {

    @AfterEach
    void tearDown() {
        // 防止线程复用导致的串扰
        UserUtil.remove();
    }

    @SuppressWarnings("unchecked")
    private static void injectContext(LoginContext ctx) throws Exception {
        Field f = UserUtil.class.getDeclaredField("USER_THREAD_LOCAL");
        f.setAccessible(true);
        ThreadLocal<LoginContext> tl = (ThreadLocal<LoginContext>) f.get(null);
        tl.set(ctx);
    }

    @Test
    @DisplayName("remove() 在未初始化线程上安全、不抛异常")
    void remove_onFreshThread_isSafe() {
        UserUtil.remove(); // 不应抛异常
    }

    @Test
    @DisplayName("未初始化时 getLocalUserId 安全返回 null（不抛 NPE）")
    void getLocalUserId_withoutInit_returnsNull() {
        UserUtil.remove(); // 确保线程本地为空
        assertThat(UserUtil.getLocalUserId()).isNull();
    }

    @Test
    @DisplayName("注入 LoginContext 后 userId 可正常读写")
    void setAndGetUserId_afterInject_roundTrips() throws Exception {
        injectContext(new LoginContext());

        UserUtil.setLocalUserId(10086L);

        assertThat(UserUtil.getLocalUserId()).isEqualTo(10086L);
    }

    @Test
    @DisplayName("注入 LoginContext 后 shopId / agentId 可正常读写")
    void setAndGetShopAndAgent_afterInject_roundTrips() throws Exception {
        injectContext(new LoginContext());

        UserUtil.setLocalShopId(66L);
        UserUtil.setAgentId(88L);

        assertThat(UserUtil.getLocalShopId()).isEqualTo(66L);
        assertThat(UserUtil.getAgentId()).isEqualTo(88L);
    }

    @Test
    @DisplayName("remove() 后再次读取安全地回到 null（不抛 NPE）")
    void remove_clearsContext() throws Exception {
        injectContext(new LoginContext());
        UserUtil.setLocalUserId(1L);
        assertThat(UserUtil.getLocalUserId()).isEqualTo(1L);

        UserUtil.remove();

        assertThat(UserUtil.getLocalUserId()).isNull();
    }
}
