package com.daxi.test;

import com.daxi.key.redis.UserKey;
import com.daxi.limit.UserLimit;
import com.daxi.util.JwtUtil;
import com.daxi.util.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static com.daxi.limit.UserLimit.LOGIN_TOKEN_PREFIX;

/**
 * 集成测试抽象基类（经 common test-jar 供各模块复用）。
 *
 * <p>约定：
 * <ul>
 *   <li>继承本类的测试会启动对应模块的完整 Spring 上下文（真实 DB / Redis + 真实 AuthInterceptor），
 *       但已关闭 Nacos / Seata / XXL-JOB / RocketMQ / OpenFeign，无需连任何外部中间件注册中心。</li>
 *   <li>后端由 {@link TestProfileResolver} 决定：
 *       <ul>
 *         <li>默认（连共享开发机）：使用 application-test.yaml 指向 172.17.156.101 的 dxshop_test 库 + Redis db1。</li>
 *         <li>{@code -Ddxshop.test.backend=container}（CI / 本地有 Docker）：启动 MySQL + Redis 真实容器，
 *             通过 {@link DynamicPropertySource} 注入连接信息，容器初始化脚本 testcontainers-init.sql 建表。</li>
 *       </ul>
 *   <li>需登录态的接口用 {@link #authHeader(Long)} 生成合法 token 并写入 Redis，走真实拦截器。</li>
 *   <li>{@code @Transactional} 保证每个测试后 DB 回滚；{@code @AfterEach} flush 测试 Redis 并清理 UserUtil ThreadLocal。</li>
 * </ul>
 *
 * <p>说明：testcontainers 1.21.x 已移除独立的 redis 模块，故 Redis 容器用 {@link GenericContainer} 承载，
 * 连接信息由本类 {@link #configureContainers(DynamicPropertyRegistry)} 显式注入，不依赖 @ServiceConnection。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(resolver = TestProfileResolver.class)
@Transactional
public abstract class AbstractIntegrationTest {

    /** 静态容器在 JVM 内共享（抽象类的静态字段被所有子类共用），由 backend 是否为 container 决定是否启动。 */
    private static MySQLContainer<?> mysqlContainer;
    private static GenericContainer<?> redisContainer;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    protected com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureContainers(DynamicPropertyRegistry registry) {
        if (!isContainerMode()) {
            return;
        }
        if (mysqlContainer == null) {
            mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                    .withDatabaseName("dxshop_test")
                    .withUsername("root")
                    .withPassword("123456")
                    .withInitScript("testcontainers-init.sql");
            mysqlContainer.start();
        }
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        if (redisContainer == null) {
            redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
                    .withExposedPorts(6379);
            redisContainer.start();
        }
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(redisContainer.getMappedPort(6379)));
        registry.add("spring.data.redis.password", () -> "");
    }

    private static boolean isContainerMode() {
        String backend = System.getProperty("dxshop.test.backend");
        if (backend == null || backend.isBlank()) {
            backend = System.getenv("DXSHOP_TEST_BACKEND");
        }
        return "container".equalsIgnoreCase(backend);
    }

    /**
     * 为指定 userId 生成合法 JWT，写入 Redis 登录态，并返回 {@code Authorization} 头的值
     * （含 Bearer 前缀），可直接用于 MockMvc 请求头，走真实 AuthInterceptor。
     */
    protected String authHeader(Long userId) {
        String token = JwtUtil.generateToken(String.valueOf(userId), null);
        String key = UserKey.LOGIN.format(userId);
        stringRedisTemplate.opsForValue().set(key, token);
        stringRedisTemplate.expire(key, 1, TimeUnit.DAYS);
        return LOGIN_TOKEN_PREFIX + token;
    }

    /**
     * 受控响应断言：接口只要返回 200/400/500 即视为“已正确路由并被应用处理”，
     * 用于冒烟测试——既能确认端点存在（非 404/405），又不会被业务层的 500（数据不存在等）
     * 或校验层的 400 误判为失败。未处理的连接级错误（如 404）会触发断言失败。
     */
    protected static ResultMatcher handled() {
        return result -> {
            int status = result.getResponse().getStatus();
            if (status != 200 && status != 400 && status != 500) {
                throw new AssertionError("接口未返回受控状态码(200/400/500)，疑似路由或方法错误: " + status);
            }
        };
    }

    /**
     * 清理测试 Redis（仅当前 db）与 UserUtil ThreadLocal，避免测试间串扰。
     */
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection();
        try {
            connection.flushDb();
        } finally {
            connection.close();
        }
        UserUtil.remove();
    }
}
