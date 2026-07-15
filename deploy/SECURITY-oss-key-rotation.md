# 阿里云 OSS 密钥：模板化现状 + 轮换清单

> 本文件不含任何真实密钥，可随仓库提交。

## 一、当前状态（已完成模板化）

| 项目 | 状态 |
|---|---|
| 4 个模块 `src/main/resources/application.yaml` 的 OSS 段 | ✅ 已全部改为 `${ALIYUN_OSS_*}` 占位符，无明文 |
| `docker-compose.yml` 4 个应用服务 | ✅ 已 `ALIYUN_OSS_ACCESS_KEY_ID: ${ALIYUN_OSS_ACCESS_KEY_ID}` 等从 `.env` 注入 |
| `.env.example` | ✅ 5 个 OSS 变量齐全，AK/SK/RoleArn 均为 `__FILL_IN__` 占位符 |
| git 版本库历史 | ✅ 经 `git log -S` 全历史核验，**明文 AK/SK 从未进过版本库**；`application.yaml` 一直被 `.gitignore` 的 `application*.yaml` 规则忽略 |
| 本地 `target/` 编译缓存 | ✅ 曾残留旧明文（chat/order 的 `target/classes/application.yaml`），已删除；下次 `mvn package` 会从模板重新生成为占位符版 |

**结论**：因为密钥从未推送到 GitHub，**无需做 git 历史清洗（BFG / filter-repo）**。

## 二、配置读取链路（供理解）

```
应用启动
  └─ OssProperties(@ConfigurationProperties "aliyun.oss")  ← application.yaml
        └─ endpoint/bucket-name/access-key-id/access-key-secret/role-arn
             ← ${ALIYUN_OSS_*} 环境变量
                  ← docker-compose.yml environment
                       ← /opt/dxshop/.env（服务器侧手动填，不入库）
  └─ OssConfig.ossClient()  用 endpoint + ak-id + ak-secret 构建 OSS 单例
  └─ OssUtil  用 ak/sk 签名直传策略 & STS AssumeRole（role-arn）+ 私有 URL 预签名
```

- `access-key-id` / `access-key-secret`：**无默认值**，必须由 env 提供（缺失则启动即报 `InvalidCredentialsException`）。
- `endpoint` / `bucket-name` / `role-arn`：有默认值（`oss-cn-wuhan-lr.aliyuncs.com` / `daxi-shop` / `acs:ram::1157467789361533:role/daxi-shopuploadandquery`），可被 env 覆盖。

## 三、是否需要轮换？

密钥虽未进 git，但满足以下**任一**条件时建议轮换：
- 该 AK/SK 曾以明文形式长期存在于本地源码 / 发给过他人 / 出现在截图或聊天记录；
- 仓库曾经是 public，或有非可信人员接触过本地工作区；
- 你无法 100% 确认这对长期静态密钥从未离开过可信范围。

> 鉴于这对密钥此前长期硬编码在源码里，**推荐轮换一次**作为干净起点。

## 四、轮换步骤（阿里云控制台）

1. **创建新 RAM 用户 AK/SK**（不要用主账号 AK）
   - 控制台 → RAM 访问控制 → 用户 → 创建用户（编程访问）→ 记录新的 `AccessKeyId` / `AccessKeySecret`。
   - 给该用户授权：`AliyunOSSFullAccess`（或按最小权限自定义仅限 `daxi-shop` bucket 的读写 + `sts:AssumeRole`）。
2. **确认 RAM 角色可被新用户 assume**
   - RAM → 角色 → `daxi-shopuploadandquery` → 信任策略里允许该 RAM 用户 `sts:AssumeRole`。
   - 记录角色 ARN（形如 `acs:ram::<账号ID>:role/daxi-shopuploadandquery`）。
3. **更新服务器 `.env`**（唯一存放真实密钥处）
   ```bash
   vi /opt/dxshop/.env
   # 填入：
   # ALIYUN_OSS_ACCESS_KEY_ID=<新AK>
   # ALIYUN_OSS_ACCESS_KEY_SECRET=<新SK>
   # ALIYUN_OSS_ROLE_ARN=acs:ram::<账号ID>:role/daxi-shopuploadandquery
   ```
4. **重启应用让新密钥生效**
   ```bash
   cd /opt/dxshop && docker compose up -d --force-recreate dxshop-user dxshop-order dxshop-goods dxshop-chat
   ```
5. **验证新密钥可用**
   - 调 `POST /api/oss/upload?key=test/x.png` 应返回上传策略（含 STS token）。
   - 前端上传一张图 → 能生成私有访问 URL 并正常展示。
6. **停用旧密钥**（验证通过后）
   - RAM → 原 AccessKey → **先禁用**（观察 1–2 天无异常）→ **再删除**。

## 五、本地开发者注意

- 本地跑应用时，OSS 变量从本机 `.env` / IDE 运行配置 / 环境变量读取；`.env` 已被 gitignore，切勿改回硬编码。
- 集成测试（`application-test.yaml` / `application-testcontainers.yaml`）用的是占位符（`test-access-key-id` 等），`OssUtil` 由 `@MockBean` 隔离，不会真实调用 OSS，无需真实密钥。

## 六、检查清单（轮换后自查）

- [ ] `grep -rn "LTAI" --exclude-dir=.git .` 无任何输出（本地无旧明文）
- [ ] 服务器 `.env` 已填新 AK/SK，且文件权限 `chmod 600 /opt/dxshop/.env`
- [ ] 旧 AccessKey 在 RAM 控制台已禁用/删除
- [ ] `/api/oss/upload` 冒烟通过、前端上传/预览正常
