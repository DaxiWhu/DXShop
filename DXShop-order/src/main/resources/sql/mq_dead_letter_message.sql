-- MQ死信消息记录表
-- 用于记录进入死信队列的消息，便于人工介入处理

CREATE TABLE IF NOT EXISTS `mq_dead_letter_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `msg_id` VARCHAR(64) NOT NULL COMMENT '消息ID',
  `topic` VARCHAR(128) NOT NULL COMMENT 'Topic名称',
  `tag` VARCHAR(64) DEFAULT NULL COMMENT 'Tag标签',
  `consumer_group` VARCHAR(128) NOT NULL COMMENT '消费者组',
  `business_key` VARCHAR(128) DEFAULT NULL COMMENT '业务键（订单号/退款ID等）',
  `message_body` TEXT COMMENT '消息体内容',
  `retry_count` INT DEFAULT 0 COMMENT '重试次数',
  `error_message` TEXT COMMENT '错误信息',
  `business_type` VARCHAR(64) NOT NULL COMMENT '业务类型：ORDER_TIMEOUT-订单超时, REFUND_TIMEOUT-退款超时, ADDRESS_MODIFY-地址修改超时',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理, 1-已处理, 2-忽略',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `handler` VARCHAR(64) DEFAULT NULL COMMENT '处理人',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `handle_remark` TEXT COMMENT '处理备注',
  PRIMARY KEY (`id`),
  KEY `idx_msg_id` (`msg_id`) COMMENT '消息ID索引',
  KEY `idx_business_key` (`business_key`) COMMENT '业务键索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
  KEY `idx_business_type` (`business_type`) COMMENT '业务类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ死信消息记录表';
