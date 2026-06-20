package com.daxi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.daxi.domain.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
