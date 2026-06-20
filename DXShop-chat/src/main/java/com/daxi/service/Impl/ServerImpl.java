package com.daxi.service.Impl;

import cn.hutool.core.util.IdUtil;
import com.daxi.domain.entity.ChatMessage;
import com.daxi.domain.entity.ChatSession;
import com.daxi.mapper.ChatMessageMapper;
import com.daxi.mapper.ChatSessionMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServerImpl {
    private final @NonNull ChatMessageMapper chatMessageMapper;
    private final @NonNull ChatSessionMapper chatSessionMapper;
    public ChatMessage saveMessage(Long sessionId, int senderType, Long senderId, String content) {
        ChatMessage message = new ChatMessage();
        message.setId(IdUtil.getSnowflake(1, 1).nextId());
        message.setSessionId(sessionId);
        message.setSenderType(senderType);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);
        return message;
    }
    public void updateSessionAfterMessage(ChatSession session, String content) {
        String summary = content.length() > 50 ? content.substring(0, 50) : content;
        session.setLastMessage(summary);
        session.setLastMessageTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);
    }
}
