#!/usr/bin/env groovy
/*
 * DXShop —— Jenkins + Ansible CI/CD 流水线
 *
 * 适用场景：无公网 IP 的内网机器（本例为 WSL / Linux），Jenkins 装在本机，
 *          通过「SCM 轮询」触发（每 2 分钟查一次 GitHub 是否有新提交），
 *          无需 GitHub Webhook 打进内网。
 * 流程：Checkout → 编译&测试(mvn verify) → 本地构建 4 个微服务镜像 → Ansible 部署。
 * 镜像在 Jenkins 本机本地构建（dxshop-*:latest），不经过 GHCR，
 * 规避内网拉取镜像时的代理 / 网络问题。
 */

pipeline {
    agent any

    triggers {
        // 无公网 IP：用 SCM 轮询替代 Webhook（对应 GitHub Actions 的 push 触发）
        pollSCM('H/2 * * * *')
    }

    environment {
        // 本地镜像前缀（不带仓库地址，纯本地 tag）
        IMAGE_PREFIX = 'dxshop'
        // 镜像 tag：用 commit 短哈希便于回滚；同时打 latest 供 compose 引用
        IMAGE_TAG = "${env.GIT_COMMIT?.take(7) ?: 'latest'}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 60, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh './mvnw -B -ntp clean verify -Ddxshop.test.backend=container'
            }
        }

        stage('Build Images') {
            steps {
                script {
                    def modules = [
                        [name: 'user',  dockerfile: 'Dockerfile.user'],
                        [name: 'order', dockerfile: 'Dockerfile.order'],
                        [name: 'goods', dockerfile: 'Dockerfile.goods'],
                        [name: 'chat',  dockerfile: 'Dockerfile.chat'],
                    ]
                    for (m in modules) {
                        def img = "${IMAGE_PREFIX}-${m.name}"
                        echo "构建镜像 ${img}:${IMAGE_TAG} 与 ${img}:latest"
                        sh "docker build -f ${m.dockerfile} -t ${img}:${IMAGE_TAG} -t ${img}:latest ."
                    }
                }
            }
        }

        stage('Deploy (Ansible)') {
            steps {
                sh "ansible-playbook -i ansible/inventory.ini ansible/deploy.yml --extra-vars \"image_tag=${IMAGE_TAG}\""
            }
        }
    }

    post {
        failure {
            echo '流水线失败，请查看上方日志。常见原因：.env 未配置、端口被占用、镜像构建失败、Ansible 目标不可达。'
        }
        success {
            echo "部署完成：DXShop 已通过 Ansible 在目标机以 docker compose 启动（镜像 tag=${IMAGE_TAG}）。"
        }
    }
}
