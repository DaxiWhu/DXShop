# DXShop —— WSL/Linux 原生安装 Jenkins + Ansible 全流程教程

> 场景：无公网 IP 的内网机器（WSL2 / Linux 宿主机），**原生安装**（非 Docker）Jenkins 和 Ansible。
> 镜像在本机本地构建（`dxshop-*:latest`），不经 GHCR；用 **SCM 轮询** 触发，绕开 Webhook 入站。
>
> 全程照抄命令即可。凡是 `# ← 说明` 都是给你看的注释，不用输入。

---

## 0. 前提检查（30 秒）

```bash
# 确认在 WSL/Linux 里，且能出站访问 GitHub
uname -a
ping -c 2 github.com
# 确认已装 Docker（没装见附录 A）
docker version && docker compose version
```

你这套方案需要 **两个 JDK**：
- **JDK 21** —— Jenkins 运行时（apt 装最省事）
- **JDK 25** —— 项目 `./mvnw verify` 按 25 编译（`pom.xml` 里 `java.version=25`）

---

## 1. 安装 JDK 21（Jenkins 运行时）

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
java -version   # 应显示 openjdk version "21..."
```

---

## 2. 安装 JDK 25（项目构建用）

Ubuntu 仓库通常还没有 openjdk-25，用官方 Temurin tarball 手动装：

```bash
cd /tmp
# 下载 Temurin JDK 25（x64）。若此链接失效，去 https://adoptium.net/temurin/releases/?version=25 取最新 tar.gz 地址
curl -fsSL -o jdk25.tar.gz \
  "https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25%2B36/OpenJDK25U-jdk_x64_linux_hotspot_25_36.tar.gz"

sudo mkdir -p /opt/jdk-25
sudo tar -xzf jdk25.tar.gz -C /opt/jdk-25 --strip-components=1
/opt/jdk-25/bin/java -version   # 应显示 openjdk version "25..."
```

> 记住路径 **`/opt/jdk-25`**，第 6 步在 Jenkins 里要把它配成"构建用 JDK"。

---

## 3. 安装 Ansible

```bash
sudo apt install -y ansible
ansible --version   # 确认能跑
```

---

## 4. 安装 Jenkins（官方 apt 源）

```bash
# 4.1 导入官方 key
sudo mkdir -p /usr/share/keyrings
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key \
  | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null

# 4.2 添加软件源
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" \
  | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null

# 4.3 安装
sudo apt update
sudo apt install -y jenkins
```

---

## 5. 启动 Jenkins（WSL 分两种情况）

### 情况 A：你的 WSL 已开启 systemd（推荐）

先确认：`cat /etc/wsl.conf` 里若有 `[boot]\nsystemd=true` 就是已开。

```bash
sudo systemctl enable jenkins
sudo systemctl start jenkins
sudo systemctl status jenkins   # 看到 active (running) 即成功
```

### 情况 B：WSL 没开 systemd

**方案 B1（推荐）：开启 systemd 再走情况 A**

```bash
sudo tee /etc/wsl.conf > /dev/null <<'EOF'
[boot]
systemd=true
EOF
```
然后在 **Windows PowerShell** 里执行 `wsl --shutdown`，重开 WSL，再回情况 A。

**方案 B2：不改 systemd，直接前台 / 后台跑 war**

```bash
# 前台跑（关终端就停，适合调试）
sudo -u jenkins java -jar /usr/share/jenkins/jenkins.war --httpPort=8080

# 或后台跑（nohup，日志到 jenkins.out）
sudo -u jenkins bash -c 'nohup java -jar /usr/share/jenkins/jenkins.war --httpPort=8080 > /var/lib/jenkins/jenkins.out 2>&1 &'
```

---

## 6. 首次进 Jenkins + 配置两个 JDK

### 6.1 打开网页

- WSL 里访问：先拿 WSL 的 IP：`hostname -I`（取第一个，如 `172.20.x.x`）
- Windows 浏览器打开 `http://172.20.x.x:8080`（localhost 有时不通，用这个 IP）

### 6.2 解锁

```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```
粘进网页 → 选 **Install suggested plugins** → 建管理员账号。

### 6.3 让 Jenkins 用户能调 Docker（关键！）

playbook 没有 `become`，靠 Jenkins 用户在 docker 组免 sudo：

```bash
sudo usermod -aG docker jenkins
# 重启 Jenkins 让组生效
sudo systemctl restart jenkins   # 情况 A
# 情况 B：kill 掉 war 进程再重新 nohup 启动
```

### 6.4 配置 JDK 25 为"构建用 JDK"

**Manage Jenkins → Tools → JDK installations → Add JDK**：
- Name：`jdk25`
- 取消勾选 "Install automatically"
- JAVA_HOME：`/opt/jdk-25`
- Save

> 注：本项目 `Jenkinsfile` 用 `./mvnw` 且没在里面显式切 JDK。最稳妥是**在 Jenkinsfile 的 Build 阶段前导出 `JAVA_HOME=/opt/jdk-25`**（见第 9 步"可选加固"），或用下面第 7 步的 Global 环境变量兜底。

---

## 7. 准备部署目录和 .env（关键，不做 playbook 会失败）

```bash
sudo mkdir -p /opt/dxshop
# 把仓库里的 .env.example 复制过去（路径换成你的仓库位置）
sudo cp /path/to/DXShop/.env.example /opt/dxshop/.env
sudo chmod 600 /opt/dxshop/.env
# 让 Jenkins 用户可写整个部署目录（playbook 不以 root 跑）
sudo chown -R jenkins:jenkins /opt/dxshop

# 编辑填真实值
sudo -u jenkins vim /opt/dxshop/.env
```

`.env` 里必须填的真实值：
- `MYSQL_ROOT_PASSWORD`
- `REDIS_PASSWORD`
- `NACOS_AUTH_TOKEN`（建议随机长串）
- `ALIYUN_OSS_ACCESS_KEY_ID` / `ALIYUN_OSS_ACCESS_KEY_SECRET` / `ALIYUN_OSS_ROLE_ARN`

---

## 8. 推代码 + 建 Jenkins 流水线任务

### 8.1 先把代码推上去（否则 Jenkins 拉不到）

```bash
cd /path/to/DXShop
git push origin waitTest
```

### 8.2 网页建任务

1. **New Item** → 名字 `dxshop-deploy` → 选 **Pipeline** → OK。
2. 拉到 **Pipeline** 段：
   - Definition：`Pipeline script from SCM`
   - SCM：`Git`
   - Repository URL：你的仓库地址（HTTPS 或 SSH）
   - Credentials：仓库私有才需加（GitHub PAT 或 SSH key）
   - Branch Specifier：`*/waitTest`
   - Script Path：`Jenkinsfile`
3. **Build Triggers** → 勾 **Poll SCM**，日程 `H/2 * * * *`（每 2 分钟查一次）。
4. Save。

### 8.3 首次手动触发

点 **Build Now** → 进 **#1 → Console Output** 看日志。
四个阶段应依次绿：`Checkout → Build & Test → Build Images → Deploy (Ansible)`。

---

## 9. 验证部署结果

```bash
docker compose -f /opt/dxshop/docker-compose.yml ps
# 期望：12 个容器都 Up；mysql/redis/nacos 显示 healthy
```

之后你每次 `git push origin waitTest`，最多 2 分钟 Jenkins 自动拉取并跑完整条流水线。

### 可选加固：在 Jenkinsfile 里锁死 JDK25 构建

若构建阶段报 JDK 版本不符，在 `Jenkinsfile` 的 `Build & Test` 阶段把 mvn 换成带 JAVA_HOME：

```groovy
stage('Build & Test') {
    steps {
        sh 'JAVA_HOME=/opt/jdk-25 ./mvnw -B -ntp clean verify'
    }
}
```

---

## 附录 A：没装 Docker 的话

```bash
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable --now docker   # 或 WSL 无 systemd 时 sudo service docker start
sudo usermod -aG docker $USER         # 当前用户也进 docker 组，重开终端生效
```

## 附录 B：国内网络给 Docker 配加速/代理（构建/拉中间件镜像慢时）

拉中间件镜像（mysql/redis/nacos 等）走的是 **Docker daemon**，代理要配在 daemon 层：

```bash
sudo mkdir -p /etc/systemd/system/docker.service.d
sudo tee /etc/systemd/system/docker.service.d/http-proxy.conf > /dev/null <<'EOF'
[Service]
Environment="HTTP_PROXY=http://172.x.x.1:7890"
Environment="HTTPS_PROXY=http://172.x.x.1:7890"
Environment="NO_PROXY=localhost,127.0.0.1"
EOF
sudo systemctl daemon-reload && sudo systemctl restart docker
docker info | grep -i proxy   # 确认生效
```
> `172.x.x.1` 换成 Windows 宿主机在 WSL 网段的 IP（`ip route | grep default` 的网关）。代理软件需开 Allow LAN。

## 附录 C：常见坑速查

| 现象 | 原因 | 解法 |
|------|------|------|
| Deploy 阶段报 `/opt/dxshop/.env 不存在` | 没做第 7 步 | `cp .env.example .env` 填值 |
| `docker: permission denied` | Jenkins 用户不在 docker 组 | 第 6.3 步 + 重启 Jenkins |
| `docker compose` 落在容器里/找不到宿主 | （仅 Docker 装才有）原生装无此坑 | —— |
| Build 阶段 JDK 版本报错 | 用了 JDK21 编译 25 项目 | 第 9 步锁 `JAVA_HOME=/opt/jdk-25` |
| 网页 localhost:8080 打不开 | WSL 网络隔离 | 用 `hostname -I` 的 IP 访问 |
| 端口 8080 被占 | 别的服务占用 | `--httpPort=8081` 或改 `/etc/default/jenkins` |
| 中间件镜像拉取超时/EOF | 国内网络 | 附录 B 配 daemon 代理 |

---

## 一页速查（TL;DR）

```bash
# 1. 两个 JDK
sudo apt install -y openjdk-21-jdk ansible
# JDK25 手动解压到 /opt/jdk-25（见第 2 步）

# 2. Jenkins
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] https://pkg.jenkins.io/debian-stable binary/" | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt update && sudo apt install -y jenkins
sudo systemctl enable --now jenkins        # WSL 无 systemd 见第 5 步 B

# 3. 权限 + 部署目录
sudo usermod -aG docker jenkins && sudo systemctl restart jenkins
sudo mkdir -p /opt/dxshop && sudo cp /path/to/DXShop/.env.example /opt/dxshop/.env
sudo chmod 600 /opt/dxshop/.env && sudo chown -R jenkins:jenkins /opt/dxshop
sudo -u jenkins vim /opt/dxshop/.env       # 填真实值

# 4. 解锁密码 + 网页建 Pipeline（Poll SCM: H/2 * * * *，Script Path: Jenkinsfile）
sudo cat /var/lib/jenkins/secrets/initialAdminPassword

# 5. 推代码触发
git push origin waitTest
```
