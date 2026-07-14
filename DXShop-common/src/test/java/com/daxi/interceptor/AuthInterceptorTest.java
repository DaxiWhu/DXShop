package com.daxi.interceptor;

import com.daxi.annotation.AnonymousAccess;
import com.daxi.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * AuthInterceptor 单元测试：mock 掉 StringRedisTemplate，不连真实 Redis。
 * 覆盖 OPTIONS 放行、@AnonymousAccess 放行、无 Token 放行、Token 校验分支。
 *
 * 说明：该拦截器设计偏"宽松"——preHandle = userLogin() || agentLogin()，
 * 而 agentLogin() 在无 agent 头时恒返回 true，故大多数情况整体放行，
 * 真正的强校验发生在 Controller 内部。测试如实覆盖其分支。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthInterceptor 单元测试")
class AuthInterceptorTest {

    @Mock
    StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    AuthInterceptor interceptor;

    /** 用于构造 HandlerMethod 的测试控制器 */
    static class DummyController {
        @AnonymousAccess
        public void anonymous() {
        }

        public void secured() {
        }
    }

    private HandlerMethod handler(String methodName) throws Exception {
        return new HandlerMethod(new DummyController(),
                DummyController.class.getMethod(methodName));
    }

    @Test
    @DisplayName("OPTIONS 预检请求直接放行，不访问 Redis")
    void optionsRequest_isPassedWithoutRedis() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("OPTIONS");

        boolean result = interceptor.preHandle(req, new MockHttpServletResponse(), new Object());

        assertThat(result).isTrue();
        verifyNoInteractions(stringRedisTemplate);
    }

    @Test
    @DisplayName("@AnonymousAccess 方法直接放行，不访问 Redis")
    void anonymousAccessMethod_isPassedWithoutRedis() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");

        boolean result = interceptor.preHandle(req, new MockHttpServletResponse(), handler("anonymous"));

        assertThat(result).isTrue();
        verifyNoInteractions(stringRedisTemplate);
    }

    @Test
    @DisplayName("受保护方法但无 Authorization 头：按宽松设计放行")
    void securedMethodWithoutToken_isPassed() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");

        boolean result = interceptor.preHandle(req, new MockHttpServletResponse(), handler("secured"));

        assertThat(result).isTrue();
        verifyNoInteractions(stringRedisTemplate);
    }

    @Test
    @DisplayName("携带有效 Token 但与 Redis 不匹配：查询了 Redis，userLogin 分支判定失败")
    void tokenNotMatchingRedis_queriesRedis() throws Exception {
        String jwt = JwtUtil.generateToken("10086", new HashMap<>());
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setMethod("GET");
        req.addHeader("Authorization", "Bearer " + jwt);

        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(ops);
        when(ops.get("user:login:10086")).thenReturn("some-other-token");

        boolean result = interceptor.preHandle(req, new MockHttpServletResponse(), handler("secured"));

        // 验证确实以正确的 key 查询了 Redis（证明进入了 user-token 校验分支）
        verify(ops).get("user:login:10086");
        // 整体仍放行（agentLogin 无 agent 头恒 true），符合当前宽松设计
        assertThat(result).isTrue();
    }
}
