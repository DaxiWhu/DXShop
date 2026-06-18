# MQ死信队列监控方案

## 📖 文档导航

本方案提供了完整的RocketMQ死信队列监控和告警功能。

### 🚀 快速开始

**新手建议从这里开始** → [快速开始.md](./快速开始.md)

5分钟完成部署，立即生效！

### 📚 详细文档

1. **[实施方案总结.md](./实施方案总结.md)**
   - 方案概述和架构设计
   - 文件清单和核心代码说明
   - 数据库设计和API接口
   - 验收标准和监控指标
   - 后续优化方向

2. **[MQ死信队列监控方案说明.md](./MQ死信队列监控方案说明.md)**
   - 完整的功能说明
   - 详细的部署步骤
   - 人工处理流程
   - 常见问题解答

3. **[MQ死信队列测试指南.md](./MQ死信队列测试指南.md)**
   - 多种测试方案
   - 接口测试示例
   - 钉钉告警测试
   - 性能测试方法
   - 故障排查指南

## 🎯 核心功能

### 1. 自动监听
- 监听3个业务的死信队列
- 订单超时、退款超时、地址修改超时

### 2. 持久化记录
- 所有死信消息保存到数据库
- 完整的消息信息和错误详情
- 便于追溯和分析

### 3. 实时告警
- 钉钉机器人告警（可配置）
- 日志记录（双重保障）
- 包含所有关键信息

### 4. 管理接口
- 分页查询死信消息
- 标记为已处理/忽略
- 批量删除历史数据
- 统计未处理数量

## 📊 架构图

```
正常消费者失败 → RocketMQ重试(16次) → 进入死信队列
                                              ↓
                                    死信消费者监听
                                              ↓
                                    记录数据库 + 发送告警
                                              ↓
                                    管理员收到通知并处理
```

## 🛠️ 技术栈

- **消息队列**: RocketMQ
- **数据库**: MySQL + MyBatis Plus
- **告警**: 钉钉机器人（可扩展）
- **框架**: Spring Boot

## 📁 项目结构

```
DXShop-order/
├── src/main/java/com/daxi/
│   ├── config/
│   │   └── HttpClientConfig.java                    # HTTP客户端配置
│   ├── controller/
│   │   └── MqDeadLetterController.java              # 管理API
│   ├── domain/entity/
│   │   └── MqDeadLetterMessage.java                 # 实体类
│   ├── mapper/order/
│   │   └── MqDeadLetterMessageMapper.java           # Mapper
│   ├── mq/consumer/
│   │   ├── DlqOrderTimeoutConsumer.java             # 订单死信消费者
│   │   ├── DlqRefundTimeoutConsumer.java            # 退款死信消费者
│   │   └── DlqAddressModifyTimeoutConsumer.java     # 地址修改死信消费者
│   └── service/
│       ├── MqAlertService.java                      # 告警服务接口
│       └── impl/MqAlertServiceImpl.java             # 告警服务实现
├── src/main/resources/
│   ├── sql/mq_dead_letter_message.sql               # 建表SQL
│   └── application.yaml                             # 配置文件
├── 快速开始.md                                       # ⭐ 从这里开始
├── 实施方案总结.md                                   # 实施总结
├── MQ死信队列监控方案说明.md                         # 详细说明
└── MQ死信队列测试指南.md                             # 测试指南
```

## ⚡ 快速部署

### 1. 执行建表SQL

```bash
mysql -h 172.17.156.101 -u root -p123456 dxshop < src/main/resources/sql/mq_dead_letter_message.sql
```

### 2. 配置钉钉告警（可选）

```yaml
mq:
  alert:
    enabled: true
    dingtalk:
      webhook: https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN
```

### 3. 重启应用

```bash
mvn clean package -DskipTests
java -jar target/DXShop-order.jar
```

### 4. 验证部署

```bash
curl http://localhost:8080/api/mq/dead-letter/unprocessed-count
```

## 📱 使用示例

### 查询未处理数量

```bash
curl http://localhost:8080/api/mq/dead-letter/unprocessed-count
```

### 分页查询

```bash
curl "http://localhost:8080/api/mq/dead-letter/page?pageNum=1&pageSize=10&status=0"
```

### 标记为已处理

```bash
curl -X POST "http://localhost:8080/api/mq/dead-letter/handle/1?handler=张三&remark=已手动处理"
```

## 🔍 监控指标

### 建议监控

- **未处理数量**: 应该保持为0
- **日新增数量**: 正常情况下应该很少
- **平均处理时长**: 目标 < 24小时

### 监控SQL

```sql
-- 未处理数量
SELECT COUNT(*) FROM mq_dead_letter_message WHERE status = 0;

-- 今日新增
SELECT COUNT(*) FROM mq_dead_letter_message 
WHERE DATE(create_time) = CURDATE();

-- 按业务类型统计
SELECT business_type, COUNT(*) as count 
FROM mq_dead_letter_message 
WHERE status = 0 
GROUP BY business_type;
```

## ❓ 常见问题

### Q: 没有配置钉钉会影响使用吗？

A: 不会。系统仍会记录到数据库和日志，只是不发送钉钉通知。

### Q: 死信消息会影响正常业务吗？

A: 不会。死信消息是已经重试16次失败的消息，不会影响正常消费。

### Q: 如何测试功能？

A: 可以直接插入测试数据，或等待真实业务触发。详见测试指南。

### Q: 需要定期清理数据吗？

A: 建议每月清理一次已处理的历史数据，避免表过大。

## 🎯 解决的问题

| 问题 | 解决方案 |
|-----|---------|
| 消息重试失败后无人知晓 | 自动告警通知 |
| 无法追溯历史死信消息 | 数据库持久化记录 |
| 缺乏统一处理流程 | 标准化管理接口 |
| 依赖人工巡检 | 主动推送告警 |

## 📈 方案优势

✅ **完整性**: 覆盖监听、记录、告警、处理全流程  
✅ **可靠性**: 双重保障（数据库+日志）  
✅ **易用性**: 提供完整的管理API  
✅ **可扩展**: 支持多种告警渠道  
✅ **可追溯**: 完整的处理记录  

## 🔧 维护说明

### 日常维护

1. **每日检查**: 查看未处理数量
2. **及时处理**: 收到告警后立即处理
3. **定期清理**: 每月清理历史数据
4. **每周复盘**: 统计死信消息，优化系统

### 故障排查

详见各文档中的"故障排查"章节。

## 📞 联系方式

如有问题，请联系开发团队。

---

**版本**: v1.0  
**更新日期**: 2026-05-24  
**维护团队**: DXShop开发团队
