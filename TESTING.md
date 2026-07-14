# DXShop 自动化测试指南

本项目采用**测试金字塔**：大量快速的单元测试 + 少量端到端的集成测试，并通过 GitHub Actions 持续集成。

---

## 1. 测试分层与命名约定

| 类型 | 命名 | 执行插件 | 触发阶段 | 是否需要中间件 |
| --- | --- | --- | --- | --- |
| 单元测试 | `*Test` / `*Tests` | Surefire | `mvn test` | 否（Mockito 全隔离） |
| 集成测试 | `*IT` | Failsafe | `mvn verify` | 是（DB + Redis） |

- **单元测试**：用 Mockito 隔离所有外部依赖（Mapper / Redis / Feign / 加密器），只验证业务逻辑分支。不启动 Spring 上下文，毫秒级。
  - 注意：MyBatis-Plus 的 `LambdaQueryWrapper` 需要 `TableInfo` 缓存，纯单测在 `@BeforeAll` 调用 `MyBatisPlusTestSupport.initTableInfo(...)` 预热。
- **集成测试**：`@SpringBootTest + MockMvc` 走完整的 Controller → Service → Mapper → 真实数据库链路，验证多层协作与 HTTP 契约。

> JDK 25 下 Mockito 依赖 byte-buddy，根 pom 已在 Surefire/Failsafe 的 `argLine` 中开启 `-Dnet.bytebuddy.experimental=true`。

---

## 2. 集成测试的两种后端模式

集成测试基类 `AbstractIntegrationTest` 通过 `TestProfileResolver` 按 `dxshop.test.backend` 属性（或 `DXSHOP_TEST_BACKEND` 环境变量）切换：

### 模式 A：共享开发机（默认，本地推荐）
- Profile：`application-test.yaml`
- 连接 `172.17.156.101` 的 `dxshop_test` 独立库 + Redis db1，速度快、无需 Docker。
- **该文件含内网凭据，已被 `.gitignore` 忽略，不入库。**
- 首次使用：复制模板 `application-test-example.yaml` → `application-test.yaml`，填入实际地址/账号。

### 模式 B：Testcontainers 真实容器（CI 用 / 本地有 Docker 时）
- Profile：`application-testcontainers.yaml`（无任何凭据，已入库）
- 由基类的静态 `MySQLContainer` / `GenericContainer(Redis)` 拉起真实容器，`@DynamicPropertySource` 注入连接信息。
- 需要本地/CI 有可用的 Docker daemon。

---

## 3. 本地如何运行

```bash
# 只跑单元测试（无需中间件、无需 Docker）
./mvnw -B test

# 跑单元 + 集成测试（默认连共享开发机）
./mvnw -B verify

# 跑单元 + 集成测试（强制 Testcontainers 容器模式，需本地 Docker）
./mvnw -B verify -Ddxshop.test.backend=container

# 单模块示例（含依赖模块一起编译）
./mvnw -B -pl DXShop-user -am verify
```

> Windows 使用 `mvnw.cmd`；Git Bash 环境如遇 classworlds 路径问题，可用仓库内 `.workbuddy/mvn.sh` 辅助脚本。

---

## 4. 持续集成（GitHub Actions）

工作流：`.github/workflows/ci.yml`

- 触发：向 `main`/`master` 推送、提 PR，或手动触发。
- 环境：`ubuntu-latest`（自带 Docker）+ Temurin JDK 25 + Maven Wrapper 3.9.9。
- 命令：`./mvnw -B -ntp verify -Ddxshop.test.backend=container`（CI 连不上共享机，强制容器模式）。
- 产物：无论成败上传 `surefire-reports` / `failsafe-reports`，保留 7 天。

---

## 5. 关键文件清单

| 文件 | 作用 |
| --- | --- |
| `DXShop-common/src/test/java/com/daxi/test/AbstractIntegrationTest.java` | 集成测试基类（容器、认证头、清理） |
| `DXShop-common/src/test/java/com/daxi/test/TestProfileResolver.java` | 按 backend 属性切换 profile |
| `DXShop-common/src/test/java/com/daxi/test/MyBatisPlusTestSupport.java` | 纯单测预热 TableInfo 缓存 |
| `DXShop-common/src/test/resources/application-test.yaml` | 共享机 profile（不入库） |
| `DXShop-common/src/test/resources/application-test-example.yaml` | 共享机 profile 模板（入库） |
| `DXShop-common/src/test/resources/application-testcontainers.yaml` | 容器 profile（入库） |
| `.github/workflows/ci.yml` | CI 工作流 |

代表性测试：
- 单元：`UserServiceImplTest`(user)、`ChatServiceImplTest`(chat)、`JwtUtilTest`/`UserUtilTest`/`AuthInterceptorTest`(common)
- 集成：`UserControllerIT`(user)、`GoodsControllerIT`(goods)、`OrderControllerIT`(order)、`DxShopChatApplicationIT`(chat 冒烟)

---

## 6. 安全提醒

- 生产 `application.yaml` 中的 OSS AK/SK、DB/Redis/Nacos 密码为**明文**，虽已被 `.gitignore` 忽略未入库，仍建议**尽快轮换并迁移到环境变量 / 配置中心加密**。
- 测试库务必使用与生产隔离的 `dxshop_test`，避免污染真实数据。
