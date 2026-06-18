# MQ死信队列监控方案使用说明

## 概述

本方案实现了RocketMQ死信队列的自动监控、记录和告警功能。当消息重试16次后仍然失败进入死信队列时，系统会自动：

1. **记录到数据库**：保存完整的消息信息和错误详情
2. **发送告警通知**：通过钉钉机器人发送告警（可配置）
3. **提供管理接口**：支持查看、处理和清理死信消息

## 架构设计

```
正常消费者处理失败 
    ↓
RocketMQ自动重试(最多16次)
    ↓
重试全部失败
    ↓
消息进入死信队列(%DLQ% + 消费者组名)
    ↓
死信消费者监听并处理
    ↓
记录数据库 + 发送告警
```

## 文件清单

### 1. 实体和Mapper
- `MqDeadLetterMessage.java` - 死信消息实体
- `MqDeadLetterMessageMapper.java` - Mapper接口

### 2. 服务层
- `MqAlertService.java` - 告警服务接口
- `MqAlertServiceImpl.java` - 告警服务实现

### 3. 死信消费者
- `DlqOrderTimeoutConsumer.java` - 订单超时死信消费者
- `DlqRefundTimeoutConsumer.java` - 退款超时死信消费者
- `DlqAddressModifyTimeoutConsumer.java` - 地址修改超时死信消费者

### 4. 管理接口
- `MqDeadLetterController.java` - 死信消息管理API

### 5. 配置
- `HttpClientConfig.java` - RestTemplate配置
- `application.yaml` - 告警配置项

### 6. 数据库
- `sql/mq_dead_letter_message.sql` - 建表SQL

## 部署步骤

### 1. 执行建表SQL

```bash
mysql -h 172.17.156.101 -u root -p dxshop < src/main/resources/sql/mq_dead_letter_message.sql
```

或者在MySQL客户端中直接执行SQL文件内容。

### 2. 配置钉钉告警（可选）

#### 方式1：环境变量配置（推荐）

```bash
export DINGTALK_WEBHOOK=https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN
```

#### 方式2：直接在application.yaml中配置

```yaml
mq:
  alert:
    enabled: true
    dingtalk:
      webhook: https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN
```

#### 获取钉钉Webhook

1. 在钉钉群中添加自定义机器人
2. 安全设置选择"加签"或"关键词"
3. 复制Webhook地址
4. 配置到系统中

### 3. 重启应用

```bash
mvn clean package
java -jar DXShop-order.jar
```

## 功能说明

### 1. 自动监控

三个死信消费者会自动监听对应的死信队列：

| 死信队列 | 业务类型 | 说明 |
|---------|---------|------|
| `%DLQ%order-timeout-consumer-group` | ORDER_TIMEOUT | 订单超时取消失败 |
| `%DLQ%refund-timeout-consumer-group` | REFUND_TIMEOUT | 退款超时关闭失败 |
| `%DLQ%address-modify-timeout-consumer-group` | ADDRESS_MODIFY_TIMEOUT | 地址修改超时关闭失败 |

### 2. 告警通知

当有消息进入死信队列时，会收到钉钉告警：

```
【DXShop】MQ死信消息告警

业务类型: 订单超时取消
消息ID: AC1400017B2E18B4AAC27F6A7C5E0000
业务键: 123456789
重试次数: 16 次
错误信息: 
订单超时处理失败，重试16次后进入死信队列，需要人工介入处理

发生时间: 2026-05-24 10:30:00

> 请尽快登录系统查看并处理！
```

同时会在日志中记录：

```
ERROR - ========== MQ死信消息告警 ==========
ERROR - 业务类型: 订单超时取消
ERROR - 消息ID: AC1400017B2E18B4AAC27F6A7C5E0000
ERROR - 业务键: 123456789
ERROR - 重试次数: 16
ERROR - 错误信息: 订单超时处理失败...
ERROR - 发生时间: 2026-05-24 10:30:00
ERROR - =====================================
```

### 3. 管理接口

#### 3.1 分页查询死信消息

```http
GET /api/mq/dead-letter/page?pageNum=1&pageSize=10&businessType=ORDER_TIMEOUT&status=0
```

参数说明：
- `pageNum`: 页码（默认1）
- `pageSize`: 每页数量（默认10）
- `businessType`: 业务类型筛选（可选）
- `status`: 状态筛选（可选，0-待处理, 1-已处理, 2-忽略）

响应示例：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

#### 3.2 获取未处理数量

```http
GET /api/mq/dead-letter/unprocessed-count
```

响应：
```json
{
  "code": 200,
  "data": 5
}
```

#### 3.3 查看详情

```http
GET /api/mq/dead-letter/{id}
```

#### 3.4 标记为已处理

```http
POST /api/mq/dead-letter/handle/{id}?handler=张三&remark=已手动取消订单
```

#### 3.5 忽略消息

```http
POST /api/mq/dead-letter/ignore/{id}?handler=李四&remark=订单已支付，无需处理
```

#### 3.6 批量删除

```http
DELETE /api/mq/dead-letter/batch-delete
Content-Type: application/json

[1, 2, 3]
```

注意：只能删除已处理(status=1)或已忽略(status=2)的消息。

## 人工处理流程

### 场景1：订单超时取消失败

**问题**：订单超过30分钟未支付，但自动取消失败

**处理步骤**：
1. 收到告警通知
2. 查看死信消息详情，获取订单号
3. 检查订单当前状态和支付状态
4. 根据情况选择处理方式：
   - 如果订单仍未支付：手动取消订单
   - 如果订单已支付：标记为"忽略"，备注"订单已支付"
5. 在系统中更新处理状态

### 场景2：退款超时关闭失败

**问题**：退款申请超时未处理，但自动关闭失败

**处理步骤**：
1. 收到告警通知
2. 查看退款申请状态
3. 手动关闭退款申请或联系相关人员处理
4. 更新处理状态

### 场景3：地址修改超时关闭失败

**问题**：地址修改申请超时未处理，但自动关闭失败

**处理步骤**：
1. 收到告警通知
2. 查看地址修改申请状态
3. 手动关闭申请或联系商家处理
4. 更新处理状态

## 监控建议

### 1. 定期检查未处理数量

建议每天检查一次未处理的死信消息数量：

```bash
curl http://localhost:8080/api/mq/dead-letter/unprocessed-count
```

如果数量持续增长，说明存在系统性问题，需要排查。

### 2. 设置告警阈值

可以在运维监控系统中添加监控项：
- 当未处理死信消息 > 10 时，发送警告
- 当未处理死信消息 > 50 时，发送严重告警

### 3. 定期清理历史数据

建议每月清理一次已处理的历史数据：

```sql
-- 删除3个月前已处理的消息
DELETE FROM mq_dead_letter_message 
WHERE status IN (1, 2) 
  AND handle_time < DATE_SUB(NOW(), INTERVAL 3 MONTH);
```

## 常见问题

### Q1: 为什么没有收到钉钉告警？

**检查项**：
1. 确认 `mq.alert.enabled=true`
2. 确认钉钉Webhook配置正确
3. 检查网络连接是否正常
4. 查看日志是否有"钉钉告警发送成功"的记录

**临时方案**：即使钉钉发送失败，日志中也会记录完整的告警信息。

### Q2: 死信消息会不会无限循环消费？

**不会**。死信消费者中不抛出异常，消息消费后会ACK，不会再次投递。

### Q3: 如何测试死信队列功能？

**方法1**：修改正常消费者的代码，故意抛出异常16次以上

**方法2**：直接向死信队列发送测试消息（需要RocketMQ控制台）

**方法3**：等待真实业务场景触发

### Q4: 能否自动处理死信消息？

**不建议**。死信消息通常是严重异常的产物，需要人工判断和处理。自动处理可能导致数据不一致。

但可以针对特定场景编写补偿脚本，例如：
- 定时扫描死信消息表
- 对于订单超时类，检查订单状态后自动处理
- 处理后自动更新状态为"已处理"

### Q5: 如何扩展其他告警渠道？

在 `MqAlertServiceImpl` 中添加新的告警方法：

```java
// 发送邮件告警
private void sendEmailAlert(MqDeadLetterMessage message) {
    // 实现邮件发送逻辑
}

// 发送短信告警
private void sendSmsAlert(MqDeadLetterMessage message) {
    // 实现短信发送逻辑
}
```

然后在 `sendAlertNotification` 方法中调用。

## 注意事项

1. **不要禁用告警**：生产环境务必保持 `mq.alert.enabled=true`
2. **及时处理**：收到告警后应尽快处理，避免影响业务
3. **保留证据**：处理前截图保存，便于后续分析
4. **定期复盘**：每周统计死信消息数量和类型，优化系统稳定性
5. **权限控制**：管理接口应添加权限验证，防止误操作

## 总结

本方案提供了完整的MQ死信队列监控解决方案：

✅ **自动化**：自动监听、记录、告警  
✅ **可靠性**：双重保障（数据库+日志）  
✅ **易用性**：提供完整的管理API  
✅ **可扩展**：支持多种告警渠道  
✅ **可追溯**：完整的处理记录  

通过本方案，可以及时发现和处理MQ消费失败问题，保障系统的最终一致性。
