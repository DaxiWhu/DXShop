package com.daxi.service;

import com.daxi.domain.dto.AgentLoginDTO;
import com.daxi.domain.dto.ChatMessageDTO;
import com.daxi.domain.dto.ChatSessionDTO;

import java.util.List;

public interface ChatService {

    AgentLoginDTO agentLogin(String username, String password);

    ChatSessionDTO createSession(Long userId, Long spuId);

    List<ChatSessionDTO> getUserSessions(Long userId);

    ChatSessionDTO getSessionDetailForUser(Long sessionId, Long userId);

    ChatMessageDTO sendUserMessage(Long sessionId, Long userId, String content);

    ChatMessageDTO sendAgentMessage(Long sessionId, Long agentId, String content);

    void closeSession(Long sessionId,Long id);

    void takeSession(Long sessionId, Long agentId);

    List<ChatSessionDTO> getPendingSessions(int page, int size);

    List<ChatSessionDTO> getAgentSessions(Long agentId, Integer status, int page, int size);

    ChatSessionDTO getSessionDetailForAgent(Long sessionId, Long agentId);
}
