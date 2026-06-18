# MQ死信队列监控 - 快速测试指南

## 前置准备

### 1. 执行建表SQL

```bash
mysql -h 172.17.156.101 -u root -p123456 dxshop < src/main/resources/sql/mq_dead_letter_message.sql
```

### 2. 配置钉钉告警（可选）

在 `application.yaml` 中配置：

```yaml
mq:
  alert:
    enabled: true
    dingtalk:
      webhook: https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN
```

或者设置环境变量：

```bash
export DINGTALK_WEBHOOK=https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

## 测试方案

### 方案1：模拟消息进入死信队列（推荐）

#### 步骤1：临时修改消费者代码

在 `OrderTimeoutConsumer.java` 的 `onMessage` 方法中，故意抛出异常：

```java
@Override
public void onMessage(OrderTimeoutMessageDTO message) {
    // 故意抛出异常，模拟处理失败
    throw new RuntimeException("模拟处理失败");
}
```

#### 步骤2：发送测试消息

```bash
curl -X POST http://localhost:8080/test/mq/generate-test \
  -H "Content-Type: application/json"
```

#### 步骤3：观察日志

等待RocketMQ重试16次（约4.5小时），或者手动加速测试：

```bash
# 实时查看日志
tail -f ./logs/DXShop-order.log | grep "订单超时"
```

#### 步骤4：验证死信记录

```bash
# 查询数据库
mysql -h 172.17.156.101 -u root -p123456 dxshop -e "SELECT * FROM mq_dead_letter_message ORDER BY create_time DESC LIMIT 5;"
```

### 方案2：直接插入测试数据（快速验证）

如果不想等待重试，可以直接插入测试数据：

```sql
INSERT INTO mq_dead_letter_message (
    msg_id, topic, tag, consumer_group, business_key, 
    message_body, retry_count, error_message, business_type, status
) VALUES (
    'TEST_MSG_001', 
    '%DLQ%order-timeout-consumer-group',
    'TAG_ORDER_TIMEOUT',
    'order-timeout-consumer-group',
    '999999999',
    '{"orderId":999999999,"userId":1,"shopId":1}',
    16,
    '测试死信消息',
    'ORDER_TIMEOUT',
    0
);
```

然后测试管理接口。

### 方案3：使用RocketMQ控制台发送死信消息

1. 登录RocketMQ Console
2. 找到死信Topic：`%DLQ%order-timeout-consumer-group`
3. 发送测试消息
4. 观察死信消费者是否接收并记录

## 接口测试

### 1. 查询未处理数量

```bash
curl http://localhost:8080/api/mq/dead-letter/unprocessed-count
```

预期响应：
```json
{
  "code": 200,
  "message": "success",
  "data": 1
}
```

### 2. 分页查询死信消息

```bash
curl "http://localhost:8080/api/mq/dead-letter/page?pageNum=1&pageSize=10"
```

### 3. 按业务类型筛选

```bash
curl "http://localhost:8080/api/mq/dead-letter/page?pageNum=1&pageSize=10&businessType=ORDER_TIMEOUT"
```

### 4. 按状态筛选

```bash
curl "http://localhost:8080/api/mq/dead-letter/page?pageNum=1&pageSize=10&status=0"
```

### 5. 查看详情

假设ID为1：

```bash
curl http://localhost:8080/api/mq/dead-letter/1
```

### 6. 标记为已处理

```bash
curl -X POST "http://localhost:8080/api/mq/dead-letter/handle/1?handler=测试人员&remark=测试处理"
```

### 7. 忽略消息

```bash
curl -X POST "http://localhost:8080/api/mq/dead-letter/ignore/1?handler=测试人员&remark=测试忽略"
```

### 8. 批量删除

先插入几条测试数据，然后：

```bash
curl -X DELETE http://localhost:8080/api/mq/dead-letter/batch-delete \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3]'
```

## 钉钉告警测试

### 1. 配置测试机器人

1. 创建钉钉测试群
2. 添加自定义机器人
3. 安全设置选择"关键词"，添加关键词：`DXShop`
4. 复制Webhook地址

### 2. 更新配置

```yaml
mq:
  alert:
    enabled: true
    dingtalk:
      webhook: https://oapi.dingtalk.com/robot/send?access_token=YOUR_TEST_TOKEN
```

### 3. 触发告警

插入测试数据或等待真实死信消息。

### 4. 验证收到告警

应该在钉钉群中收到类似这样的消息：

```
【DXShop】MQ死信消息告警

业务类型: 订单超时取消
消息ID: TEST_MSG_001
业务键: 999999999
重试次数: 16 次
错误信息: 
测试死信消息

发生时间: 2026-05-24 10:30:00

> 请尽快登录系统查看并处理！
```

## 日志验证

### 1. 查看死信记录日志

```bash
grep "MQ死信" ./logs/DXShop-order.log
```

应该看到：
```
ERROR - 【MQ死信】消息已记录到数据库, msgId: TEST_MSG_001, businessType: ORDER_TIMEOUT, businessKey: 999999999
```

### 2. 查看告警日志

```bash
grep "MQ死信消息告警" ./logs/DXShop-order.log -A 10
```

应该看到完整的告警信息。

## 清理测试数据

```sql
-- 删除所有测试数据
DELETE FROM mq_dead_letter_message WHERE msg_id LIKE 'TEST_MSG_%';

-- 或者清空整张表（谨慎操作）
TRUNCATE TABLE mq_dead_letter_message;
```

## 性能测试

### 批量插入测试

```sql
-- 插入100条测试数据
DELIMITER $$
CREATE PROCEDURE insert_test_data()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 100 DO
        INSERT INTO mq_dead_letter_message (
            msg_id, topic, consumer_group, business_key, 
            retry_count, error_message, business_type, status
        ) VALUES (
            CONCAT('TEST_', i),
            '%DLQ%order-timeout-consumer-group',
            'order-timeout-consumer-group',
            CONCAT('ORDER_', i),
            16,
            '测试数据',
            'ORDER_TIMEOUT',
            0
        );
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL insert_test_data();
DROP PROCEDURE insert_test_data;
```

### 测试分页查询性能

```bash
time curl "http://localhost:8080/api/mq/dead-letter/page?pageNum=1&pageSize=50"
```

## 验收标准

✅ 数据库表创建成功  
✅ 死信消费者正常启动  
✅ 死信消息能够被记录和告警  
✅ 管理接口能够正常查询和操作  
✅ 钉钉告警能够正常发送（如果配置了）  
✅ 日志中有完整的记录  

## 故障排查

### 问题1：死信消费者没有启动

**检查**：
```bash
grep "DlqOrderTimeoutConsumer" ./logs/DXShop-order.log
```

**解决**：
- 确认RocketMQ连接正常
- 确认消费者组名称正确

### 问题2：告警没有发送

**检查**：
```bash
grep "钉钉告警" ./logs/DXShop-order.log
```

**可能原因**：
- Webhook配置错误
- 网络连接问题
- 钉钉机器人安全设置不匹配

### 问题3：接口返回404

**检查**：
- 确认应用正常启动
- 确认Controller包扫描路径正确
- 检查URL路径是否正确

## 生产环境部署检查清单

- [ ] 数据库表已创建
- [ ] 索引已建立
- [ ] 钉钉Webhook已配置
- [ ] 告警功能已启用
- [ ] 死信消费者已启动
- [ ] 日志级别配置正确
- [ ] 监控告警阈值已设置
- [ ] 相关人员已培训
- [ ] 应急处理流程已制定
