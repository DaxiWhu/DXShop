package com.daxi.test;

import org.springframework.test.context.ActiveProfilesResolver;

/**
 * 根据系统属性 / 环境变量选择测试后端：
 * <ul>
 *   <li>{@code -Ddxshop.test.backend=container} 或 {@code DXSHOP_TEST_BACKEND=container} → 用 Docker 容器（testcontainers）</li>
 *   <li>其它（默认）→ 连共享开发机中间件（test）</li>
 * </ul>
 * CI 强制使用 container；本地默认连共享机。
 */
public class TestProfileResolver implements ActiveProfilesResolver {

    @Override
    public String[] resolve(Class<?> testClass) {
        String backend = System.getProperty("dxshop.test.backend");
        if (backend == null || backend.isBlank()) {
            backend = System.getenv("DXSHOP_TEST_BACKEND");
        }
        if ("container".equalsIgnoreCase(backend)) {
            return new String[]{"testcontainers"};
        }
        return new String[]{"test"};
    }
}
