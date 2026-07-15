# DXShop 自建 Runner 部署指南（无需公网 IP）

> 适用场景：目标服务器是**家里/公司的物理机或虚拟机，没有公网 IP**。
> 原理：在这台内网机器上安装一个 GitHub Actions **self-hosted runner**，它主动**出站**连
> GitHub 拉任务并在本机执行部署。GitHub 不需要反向连你，所以**不需要公网 IP，也不需要任何
> SSH Secret**。`cd.yml` 的 `deploy` job 已改为 `runs-on: [self-hosted, linux]`。

---

## 总览：流程长这样

```
你 git push
   │
GitHub 云端跑 CI(测试) → 绿
   │
GitHub 云端跑 CD 的 build-and-push(编译 + 推镜像到 GHCR)
   │
你内网机器上的 runner 领到 deploy 任务
   │
runner 本机: 登录 GHCR → 同步 compose/deploy → docker compose pull && up -d
   │
容器起来，部署完成（全程无需公网 IP）
```

---

## 一、服务器一次性准备

在目标机器（Linux，建议 Ubuntu 22.04+）上执行：

### 1. 装 Docker + compose 插件

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo systemctl enable --now docker
docker version && docker compose version   # 验证
```

### 2. 让当前用户免 sudo 用 docker

```bash
sudo usermod -aG docker $USER
newgrp docker    # 或重新登录一次，使组生效
docker ps        # 不报权限错即 OK
```

### 3. 建部署目录并归当前用户所有（runner 要往这里写文件）

```bash
sudo mkdir -p /opt/dxshop
sudo chown -R $USER:$USER /opt/dxshop
```

### 4. 建 `.env`（**CD 不会上传 .env，必须手动建**）

先从仓库拿一份 `.env.example`（下一步装完 runner 后，仓库会 checkout 到 runner 工作目录；
或你手动 `scp`/复制一份过来），然后：

```bash
cd /opt/dxshop
cp /path/to/repo/.env.example .env
vi .env
```

`.env` 里至少要填：

| 变量 | 填什么 |
|---|---|
| `REGISTRY_OWNER` | 你的 GitHub 用户名/组织名（**小写**），镜像地址是 `ghcr.io/<owner>/dxshop-*` |
| `MYSQL_ROOT_PASSWORD` | 自定义强密码 |
| `REDIS_PASSWORD` | 自定义强密码 |
| `NACOS_AUTH_TOKEN` | 一段随机串（建议 `openssl rand -base64 32`） |
| `ALIYUN_OSS_ACCESS_KEY_ID` / `..._SECRET` / `..._ROLE_ARN` / `..._ENDPOINT` / `..._BUCKET` | 真实阿里云 OSS 值 |

---

## 二、安装并注册 self-hosted runner

### 1. 在 GitHub 上拿到注册命令

进仓库 → **Settings** → 左侧 **Actions** → **Runners** → 右上 **New self-hosted runner**
→ 选 **Linux** / **x64**。页面会给出**带一次性 token 的命令**，形如下方（**以页面显示的为准，token 会变**）：

```bash
# 1) 下载（在服务器上，用一个专门目录）
mkdir -p ~/actions-runner && cd ~/actions-runner
curl -o actions-runner-linux-x64.tar.gz -L \
  https://github.com/actions/runner/releases/download/vX.XXX.X/actions-runner-linux-x64-X.XXX.X.tar.gz
tar xzf actions-runner-linux-x64.tar.gz

# 2) 配置（token 用 GitHub 页面上给你的那个）
./config.sh --url https://github.com/<你的用户名>/DXShop --token <页面上的一次性TOKEN>
```

`config.sh` 交互提问时：
- **runner group**：回车用默认
- **runner name**：回车用默认（机器名）
- **labels**：**这里很重要**——直接回车即可（会自带 `self-hosted`、`linux`、`x64` 标签）。
  `cd.yml` 用的是 `[self-hosted, linux]`，默认标签已包含，无需额外加。
- **work folder**：回车用默认 `_work`

### 2. 把 runner 装成后台服务（开机自启、掉线自恢复）

```bash
sudo ./svc.sh install
sudo ./svc.sh start
sudo ./svc.sh status    # 显示 active (running) 即成功
```

回到 GitHub 的 **Settings → Actions → Runners**，能看到这台 runner 显示绿色 **Idle** 就注册成功了。

---

## 三、触发部署

### 方式 A：自动（推荐）

随便推一次代码（或改个注释）：

```bash
git push origin master
```

CI 绿 → CD 的 `build-and-push` 在 GitHub 云端跑完推镜像 → `deploy` 落到你的 runner 上执行。

### 方式 B：手动

仓库 **Actions** 页 → 左侧点 **CD** → 右侧 **Run workflow** → 选 `master` → **Run workflow**。

---

## 四、验证部署结果

在服务器上：

```bash
cd /opt/dxshop
docker compose ps          # 看 dxshop-user/order/goods/chat + 中间件是否 Up
docker compose logs -f dxshop-user   # 看某个应用日志（Ctrl+C 退出）
```

或直接看 GitHub **Actions → CD → deploy** 的日志，最后一步会打印 `docker compose ps`。

---

## 五、常见问题

| 现象 | 原因 & 解决 |
|---|---|
| deploy job 一直 **Queued / 排队不动** | runner 没在线。`sudo ~/actions-runner/svc.sh status` 看是否 running；确认标签含 `self-hosted` `linux` |
| `permission denied` 写 /opt/dxshop | 第一步 3 的 `chown` 没做，或 runner 用户不是目录属主 |
| `docker compose pull` 报 **401 / denied** | GHCR 未登录成功。workflow 里已用 `GITHUB_TOKEN` 自动登录；若仍失败，手动 `echo <PAT> \| docker login ghcr.io -u <用户名> --password-stdin`（PAT 需 `read:packages`） |
| 报 **/opt/dxshop/.env 不存在** | 第一步 4 没建 `.env`，按提示 `cp .env.example .env` 并填写 |
| MySQL 里没有 `seata` 库 / 业务表 | init 脚本只在**数据卷首次为空**时执行。若之前起过，`docker compose down -v` 重来，或手动导入 `deploy/mysql-init/*.sql` |
| runner 用 docker 报权限错 | 第一步 2 的 `usermod -aG docker` 没生效，重新登录一次 |

---

## 六、与「公网 IP + SSH 部署」的区别（备忘）

| | 自建 Runner（本方案） | 公网 IP + SSH |
|---|---|---|
| 需要公网 IP | ❌ 不需要 | ✅ 需要 |
| 需要 DEPLOY_* Secrets | ❌ 不需要 | ✅ 4 个 |
| runner 位置 | 你内网机器 | GitHub 云端 |
| 部署方式 | runner 本机直接 compose | scp+ssh 远程 |
| 安全暴露面 | 小（仅出站） | 需开放 SSH 端口到公网 |

> 若日后改用有公网 IP 的云服务器，可回退到 SSH 方案（git 历史里有旧版 `cd.yml`）。
