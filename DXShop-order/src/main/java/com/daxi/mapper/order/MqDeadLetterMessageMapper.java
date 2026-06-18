package com.daxi.mapper.order;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.entity.MqDeadLetterMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * MQ死信消息记录 Mapper
 */
@Mapper
public interface MqDeadLetterMessageMapper extends BaseMapper<MqDeadLetterMessage> {
}
