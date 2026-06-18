# SLF4J + Logback 日志配置详解

## 1. SLF4J 简介

SLF4J (Simple Logging Facade for Java) 是一个为 Java 提供的简单日志门面，它允许用户在部署时插入任何日志框架（如 Logback、Log4j2 等）。

## 2. Logback 配置文件结构

### 2.1 根元素 `<configuration>`
```xml
<configuration>
    <!-- 配置内容 -->
</configuration>
```

### 2.2 引入默认配置
```xml
<include resource="org/springframework/boot/logging/logback/defaults.xml"/>
```
- 作用：引入 Spring Boot 默认的日志配置，包括默认的日志格式和变量定义

### 2.3 属性定义 `<property>`
```xml
<property name="LOG_PATH" value="${LOG_PATH:-./logs}"/>
<property name="APP_NAME" value="${spring.application.name:-DXShop}"/>
```
- `name`: 属性名称
- `value`: 属性值，可以使用环境变量或系统属性
- `${LOG_PATH:-./logs}`: 如果环境变量 LOG_PATH 不存在，则使用默认值 ./logs

## 3. Appender 配置

### 3.1 控制台输出 Appender
```xml
<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
        <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        <charset>utf8</charset>
    </encoder>
</appender>
```
- `name`: Appender 的名称，用于引用
- `class`: Appender 的实现类
- `ConsoleAppender`: 将日志输出到控制台
- `encoder`: 编码器，定义日志输出格式
- `pattern`: 日志格式模式
- `charset`: 字符编码

### 3.2 文件输出 Appender
```xml
<appender name="INFO_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_PATH}/${APP_NAME}-info.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>${LOG_PATH}/${APP_NAME}-info-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <maxHistory>30</maxHistory>
        <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
            <maxFileSize>10MB</maxFileSize>
        </timeBasedFileNamingAndTriggeringPolicy>
    </rollingPolicy>
    <encoder>
        <pattern>${FILE_LOG_PATTERN}</pattern>
        <charset>utf8</charset>
    </encoder>
    <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
        <level>INFO</level>
    </filter>
</appender>
```
- `RollingFileAppender`: 支持日志文件滚动的文件输出器
- `file`: 当前日志文件的路径
- `rollingPolicy`: 滚动策略
- `TimeBasedRollingPolicy`: 基于时间的滚动策略
- `fileNamePattern`: 滚动后的文件名模式
- `%d{yyyy-MM-dd}`: 日期格式
- `%i`: 索引号，当同一天内文件大小超过限制时使用
- `maxHistory`: 保留的历史文件数量（天数）
- `SizeAndTimeBasedFNATP`: 基于大小和时间的触发策略
- `maxFileSize`: 单个日志文件的最大大小
- `filter`: 过滤器，决定哪些日志事件会被处理
- `ThresholdFilter`: 阈值过滤器，只处理指定级别及以上的日志

### 3.3 异步 Appender
```xml
<appender name="ASYNC_CONSOLE" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="CONSOLE"/>
</appender>
```
- `AsyncAppender`: 异步输出器，提高日志性能
- `appender-ref`: 引用其他 appender

## 4. Logger 配置

### 4.1 根日志器
```xml
<root level="INFO">
    <appender-ref ref="ASYNC_CONSOLE"/>
    <appender-ref ref="ASYNC_INFO"/>
    <appender-ref ref="ASYNC_ERROR"/>
</root>
```
- `level`: 日志级别（TRACE < DEBUG < INFO < WARN < ERROR）
- `appender-ref`: 引用的 appender

### 4.2 特定包日志器
```xml
<logger name="com.daxi" level="DEBUG" additivity="false">
    <appender-ref ref="ASYNC_CONSOLE"/>
    <appender-ref ref="ASYNC_INFO"/>
    <appender-ref ref="ASYNC_ERROR"/>
</logger>
```
- `name`: 包名或类名
- `level`: 日志级别
- `additivity`: 是否继承父 logger 的 appender
  - `false`: 不继承，只使用自己定义的 appender
  - `true`: 继承父 logger 的 appender

## 5. 日志级别说明

| 级别 | 说明 | 使用场景 |
|------|------|----------|
| TRACE | 最详细的日志信息 | 调试时追踪程序执行流程 |
| DEBUG | 调试信息 | 开发和测试阶段记录详细过程 |
| INFO | 一般信息 | 记录重要的业务流程节点 |
| WARN | 警告信息 | 潜在的问题，但不影响系统运行 |
| ERROR | 错误信息 | 系统错误或异常情况 |

## 6. 日志文件位置

根据配置，日志文件将存储在以下位置：

- **相对路径**: `./logs/` （相对于应用启动目录）
- **文件名格式**:
  - 通用日志: `{APP_NAME}-info.log`
  - 错误日志: `{APP_NAME}-error.log`
  - 滚动日志: `{APP_NAME}-info-yyyy-MM-dd.i.log`

对于 DXShop 项目：
- DXShop-goods: `./logs/DXShop-goods-info.log`
- DXShop-order: `./logs/DXShop-order-info.log`
- DXShop-user: `./logs/DXShop-user-info.log`

## 7. 日志格式说明

Spring Boot 默认的日志格式包含以下信息：
- 时间戳
- 日志级别
- 进程ID
- 线程名
- Logger名称
- 日志消息

示例：
```
2023-01-01 12:00:00.123  INFO 12345 --- [main] com.daxi.controller.UserController : User login successful
```

## 8. 性能优化建议

1. **使用异步 Appender**: 减少日志对主线程的影响
2. **合理设置日志级别**: 生产环境建议使用 INFO 或 WARN 级别
3. **控制日志输出量**: 避免在循环中输出大量日志
4. **定期清理日志文件**: 通过 maxHistory 参数控制保留时间

## 9. 常见问题

### 9.1 日志文件未生成
- 检查目录权限
- 确认 LOG_PATH 环境变量是否正确设置
- 验证应用是否有写入权限

### 9.2 日志级别不生效
- 检查 logger 名称是否正确
- 确认 additivity 设置是否符合预期
- 验证是否有其他配置覆盖了当前设置

### 9.3 性能问题
- 考虑增加 AsyncAppender 的队列大小
- 减少不必要的 DEBUG 级别日志
- 使用条件日志输出