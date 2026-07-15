# DXShop · Jenkins + Ansible 部署手册

> 适用场景：**无公网 IP 的内网机器**（本例为装了 WSL2 的 Windows 或一台 Linux 服务器）。
> 用 Jenkins 在本机完成「拉代码 → 编译测试 → 本地构建镜像」，再用 Ansible 把
> `docker-compose.yml` 同步到目标机并 `docker compose up -d`。
> 镜像**本地构建、本地运行，不经过 GHCR**，规避内网拉镜像的代理/网络问题。

---

## 一、整体架构

```
GitHub (master / waitTest)
   │  git fetch（出站，SCM 轮询，无需公网 IP 入站）
   ▼
┌─ Jenkins（装在内网机器本机）──────────────────────┐
│  1. Checkout                                        │
│  2. mvn verify（编译 + 单测 + 集成测试）            │
│  3. docker build × 4 → dxshop-user/order/           │
│                         goods/chat:latest（本地）    │
│  4. ansible-playbook → 目标机                        │
└───────────────────────────┬────────────────────────┘
                            │ SSH（localhost 即本机）
                            ▼
                  /opt/dxshop/
                  ├─ docker-compose.yml   （应用镜像=本地 dxshop-*:latest）
                  ├─ .env                 （真实密钥，手动建，不入仓）
                  └─ deploy/              （mysql-init / seata 配置）
                            │
                            ▼
                  12 个容器全部 up（mysql/redis/nacos/seata/rocketmq + 4 应用）
```

**三种网络方向，只有「触发」需要特别处理：**

| 方向 | 说明 | 无公网 IP 影响 |
|------|------|----------------|
| 拉代码 | Jenkins 主动 `git fetch` GitHub | ✅ 出站，没问题 |
| 部署 | Ansible 通过 SSH 推到目标机（本机/局域网） | ✅ 内网通信，没问题 |
| 触发 | GitHub 想 push 后通知 Jenkins | ❌ Webhook 需打进内网，被 NAT 挡住 → 改用 **SCM 轮询** |

---

## 二、前提条件（一次性）

1. **JDK 25**：构建用。`mvnw` 会自动下载 Maven，但需要 `JAVA_HOME` 指向 JDK 25。
   ```bash
   # Ubuntu / WSL 示例
   sudo apt install -y openjdk-25-jdk
   export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
   ```
2. **Docker + compose 插件**：构建镜像和部署都靠它。
3. **Jenkins**：装在**同一台**内网机器（详见第三节）。
4. **Ansible**：装在 Jenkins 机器上（控制节点）。
   ```bash
   sudo apt install -y ansible
   ansible --version
   ```
5. **部署目录与 .env**：目标机上先建好。
   ```bash
   sudo mkdir -p /opt/dxshop
   sudo cp /path/to/repo/.env.example /opt/dxshop/.env
   sudo chmod 600 /opt/dxshop/.env
   sudo vim /opt/dxshop/.env   # 填入 MYSQL_ROOT_PASSWORD / REDIS_PASSWORD / NACOS_AUTH_TOKEN / ALIYUN_OSS_*
   sudo chown -R $USER:$USER /opt/dxshop   # 让 Jenkins 运行用户可写
   ```
6. **Jenkins 用户加入 docker 组**（免 sudo 跑 docker）：
   ```bash
   sudo usermod -aG docker $JENKINS_USER
   # 重新登录使其生效
   ```

> 端口要求：宿主机 `8082-8085`、`8848/9848`、`6379`、`9876/10909/10911`、`7091/8091` 空闲；
> MySQL 已避让为 `13306`（避免 WSL 自启 MySQL 占 3306）。

---

## 三、安装并配置 Jenkins

### 1. 安装
```bash
# 方式 A：原生（Debian/Ubuntu/WSL）
sudo apt update && sudo apt install -y jenkins
sudo systemctl enable --now jenkins

# 方式 B：Docker（若不想污染本机）
docker run -d --name jenkins -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts
```
首次访问 `http://<机器IP>:8080`，按提示解锁（看 `/var/lib/jenkins/secrets/initialAdminPassword`），
安装推荐插件（含 **Pipeline**、**Git**、**Poll SCM**）。

### 2. 新建 Pipeline 任务
- 新建 Item → 选 **Pipeline** → 名称如 `DXShop`。
- **Pipeline 定义** 选 **Pipeline script from SCM**。
- **SCM** 选 **Git**，填仓库 URL（HTTPS 或 SSH 均可，出站即可），
  **Branches to build** 填 `*/waitTest`（或 `*/master`）。
- **Script Path** 填 `Jenkinsfile`。
- **Build Triggers** 勾选 **Poll SCM**，填 `H/2 * * * *`（每 2 分钟查一次 GitHub 新提交）。
- 保存。

### 3. 触发
- 不用配 GitHub Webhook。每次 push 后，最多 2 分钟 Jenkins 自动拉取并跑完整流水线。
- 也可在任务页点 **Build Now** 手动触发。

> 若以后有公网 IP 或用了内网穿透（frp/cpolar），可把触发换成 GitHub Webhook（Push 事件），
> 做到秒级触发，并取消 Poll SCM。

---

## 四、验证部署是否成功

在目标机（默认即 Jenkins 机器）执行：
```bash
cd /opt/dxshop && docker compose ps
# 期望：mysql/redis/nacos/seata 为 healthy，4 个 dxshop-* 为 running/healthy
docker ps --format '{{.Names}}\t{{.Status}}'
curl -s http://localhost:8082/actuator/health   # user 服务健康检查
```
Jenkins 任务控制台最后也会打印 `部署完成` 与 `docker compose ps` 输出。

---

## 五、换机 / 加机器

- **同构换机**：照第二节把新机准备好，把 `Jenkinsfile` + `ansible/` 指到这台机即可；
  `.env` 仍需手动建（密钥不入仓）。
- **部署到多台 LAN 机器**：在 `ansible/inventory.ini` 的 `[dxshop]` 增加主机（先 `ssh-copy-id`），
  并把 Jenkins 构建阶段改成把镜像 push 到**私有仓库**（如 `registry:2` 容器或阿里云 ACR），
  同时让 `docker-compose.yml` 的应用镜像引用仓库地址。本仓库默认只覆盖「本机本地部署」。

---

## 六、与旧 GitHub Actions 方案的关系

- 旧的 `cd.yml`（GitHub Actions 构建+推 GHCR+部署）已退役，移至 `deploy/legacy/github-actions-cd.yml`。
  新方案镜像本地化、`docker-compose.yml` 应用镜像改为 `dxshop-*:latest`，与旧 `cd.yml` 已不一致。
- `.github/workflows/ci.yml`（GitHub 托管测试）仍保留作为互补；如希望 Jenkins 独家负责 CI/CD，
  可自行删除或也移入 `deploy/legacy/`。
- 业务表结构仍未自动建：换全新机器时 `deploy/mysql-init/` 仅有 `01-seata-and-undo.sql`，
  需补 `00-schema.sql`（`mysqldump -uroot -p --no-data dxshop > deploy/mysql-init/00-schema.sql`）才能空库起业务表。
