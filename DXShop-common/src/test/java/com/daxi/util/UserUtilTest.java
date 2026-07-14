package com.daxi.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserUtil 特征化单元测试（Characterization Test）。
 *
 * ⚠️ 已知隐患：UserUtil 的 ThreadLocal 使用 new ThreadLocal<>()（无 withInitial），
 * 生产代码里从未 set 过 LoginContext。因此在未初始化的线程上直接调用
 * setLocalUserId / getLocalUserId 会触发 NullPointerException。
 * 这些测试如实记录当前行为，若将来把字段改为
 *   ThreadLocal.withInitial(LoginContext::new)
 * 则 "未初始化即 NPE" 的两个用例需要相应调整。
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
    @DisplayName("[已知隐患] 未初始化时 getLocalUserId 抛 NPE")
    void getLocalUserId_withoutInit_throwsNpe() {
        UserUtil.remove(); // 确保线程本地为空
        assertThatThrownBy(UserUtil::getLocalUserId)
                .isInstanceOf(NullPointerException.class);
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
    @DisplayName("remove() 后再次读取回到未初始化的 NPE 状态")
    void remove_clearsContext() throws Exception {
        injectContext(new LoginContext());
        UserUtil.setLocalUserId(1L);
        assertThat(UserUtil.getLocalUserId()).isEqualTo(1L);

        UserUtil.remove();

        assertThatThrownBy(UserUtil::getLocalUserId)
                .isInstanceOf(NullPointerException.class);
    }
}
