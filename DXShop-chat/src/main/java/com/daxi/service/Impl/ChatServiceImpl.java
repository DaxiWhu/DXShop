package com.daxi.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.daxi.Exception.BusinessException;
import com.daxi.domain.dto.AgentLoginDTO;
import com.daxi.domain.dto.ChatMessageDTO;
import com.daxi.domain.dto.ChatSessionDTO;
import com.daxi.domain.entity.ChatMessage;
import com.daxi.domain.entity.ChatSession;
import com.daxi.domain.entity.CsAgent;
import com.daxi.mapper.ChatMessageMapper;
import com.daxi.mapper.ChatSessionMapper;
import com.daxi.mapper.CsAgentMapper;
import com.daxi.service.ChatService;
import com.daxi.util.JwtUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.daxi.limit.ChatLimit.AGENT_STATUS_ONLINE;
import static com.daxi.limit.ChatLimit.AGENT_TOKEN_PREFIX;
import static com.daxi.limit.ChatLimit.JWT_ROLE_AGENT;
import static com.daxi.limit.ChatLimit.SENDER_TYPE_AGENT;
import static com.daxi.limit.ChatLimit.SENDER_TYPE_USER;
import static com.daxi.limit.ChatLimit.SESSION_STATUS_ACTIVE;
import static com.daxi.limit.ChatLimit.SESSION_STATUS_CLOSED;
import static com.daxi.limit.ChatLimit.SESSION_STATUS_WAITING;
import static com.daxi.response.ChatResponse.AGENT_NOT_EXIST;
import static com.daxi.response.ChatResponse.AGENT_PASSWORD_ERROR;
import static com.daxi.response.ChatResponse.NOT_SESSION_OWNER;
import static com.daxi.response.ChatResponse.SESSION_ALREADY_EXISTS;
import static com.daxi.response.ChatResponse.SESSION_ALREADY_TAKEN;
import static com.daxi.response.ChatResponse.SESSION_NOT_EXIST;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final @NonNull CsAgentMapper csAgentMapper;
    private final @NonNull ChatSessionMapper chatSessionMapper;
    private final @NonNull ChatMessageMapper chatMessageMapper;
    private final @NonNull PasswordEncoder passwordEncoder;
    private final @NonNull StringRedisTemplate stringRedisTemplate;
    private final @NonNull SimpMessagingTemplate messagingTemplate;
    private final @NonNull ServerImpl server;
    @Override
    public AgentLoginDTO agentLogin(String username, String password) {
        CsAgent agent = csAgentMapper.selectOne(
                new LambdaQueryWrapper<CsAgent>().eq(CsAgent::getUsername, username));
        if (agent == null) {
            throw new BusinessException(AGENT_NOT_EXIST);
        }
        if (!passwordEncoder.matches(password, agent.getPasswordHash())) {
            throw new BusinessException(AGENT_PASSWORD_ERROR);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", JWT_ROLE_AGENT);
        String token = JwtUtil.generateToken(agent.getId().toString(), claims);
        stringRedisTemplate.opsForValue().set(String.format(AGENT_TOKEN_PREFIX, agent.getId()), token);

        agent.setStatus(AGENT_STATUS_ONLINE);
        csAgentMapper.updateById(agent);

        return AgentLoginDTO.builder()
                .agentId(agent.getId())
                .nickname(agent.getNickname())
                .avatarUrl(agent.getAvatarUrl())
                .token(token)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatSessionDTO createSession(Long userId, Long spuId) {
        //检查还存在的会话
        ChatSession existing = chatSessionMapper.selectOne(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getSpuId, spuId)
                        .ne(ChatSession::getStatus, SESSION_STATUS_CLOSED));
        if (existing != null) {
            throw new BusinessException(SESSION_ALREADY_EXISTS);
        }

        ChatSession session = new ChatSession();
        session.setId(IdUtil.getSnowflake(1, 1).nextId());
        session.setUserId(userId);
        session.setSpuId(spuId);
        session.setStatus(SESSION_STATUS_WAITING);
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.insert(session);

        //通知代理
        messagingTemplate.convertAndSend("/topic/agent/pending", toSessionDTO(session));

        return toSessionDTO(session);
    }

    @Override
    public List<ChatSessionDTO> getUserSessions(Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .orderByDesc(ChatSession::getUpdateTime));
        if(CollUtil.isEmpty(sessions)){
            return Collections.emptyList();
        }
        return sessions.stream().map(this::toSessionDTO).collect(Collectors.toList());
    }

    @Override
    public ChatSessionDTO getSessionDetailForUser(Long sessionId, Long userId) {
        LambdaQueryWrapper<ChatSession> chatSessionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatSessionLambdaQueryWrapper.eq(ChatSession::getId, sessionId)
                .eq(ChatSession::getUserId, userId);
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(SESSION_NOT_EXIST);
        }

        List<ChatMessage> messages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreateTime));

        ChatSessionDTO dto = toSessionDTO(session);
        dto.setMessages(messages.stream().map(this::toMessageDTO).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageDTO sendUserMessage(Long sessionId, Long userId, String content) {
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getId, sessionId)
                .eq(ChatSession::getUserId, userId)
                .ne(ChatSession::getStatus, SESSION_STATUS_CLOSED);
        ChatSession session = chatSessionMapper.selectOne(queryWrapper);
        if (session == null) {
            throw new BusinessException(SESSION_NOT_EXIST);
        }

        ChatMessage message = server.saveMessage(sessionId, SENDER_TYPE_USER, userId, content);
        server.updateSessionAfterMessage(session, content);

        ChatMessageDTO dto = toMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageDTO sendAgentMessage(Long sessionId, Long agentId, String content) {
        LambdaQueryWrapper<ChatSession> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatSession::getId, sessionId)
                .eq(ChatSession::getCsAgentId, agentId)
                .ne(ChatSession::getStatus, SESSION_STATUS_CLOSED);
        ChatSession session = chatSessionMapper.selectOne(queryWrapper);
        if (session == null) {
            throw new BusinessException(SESSION_NOT_EXIST);
        }

        ChatMessage message = server.saveMessage(sessionId, SENDER_TYPE_AGENT, agentId, content);
        server.updateSessionAfterMessage(session, content);

        ChatMessageDTO dto = toMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, dto);
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(Long sessionId,Long id) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(SESSION_NOT_EXIST);
        }
        if (!session.getUserId().equals(id)&&!session.getCsAgentId().equals(id)) {
            throw new BusinessException(NOT_SESSION_OWNER);
        }

        session.setStatus(SESSION_STATUS_CLOSED);
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void takeSession(Long sessionId, Long agentId) {
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(SESSION_NOT_EXIST);
        }
        if (session.getCsAgentId() != null) {
            throw new BusinessException(SESSION_ALREADY_TAKEN);
        }

        session.setCsAgentId(agentId);
        session.setStatus(SESSION_STATUS_ACTIVE);
        session.setUpdateTime(LocalDateTime.now());
        chatSessionMapper.updateById(session);
    }

    @Override
    public List<ChatSessionDTO> getPendingSessions(int page, int size) {
        Page<ChatSession> pageParam = new Page<>(page, size);
        Page<ChatSession> result = chatSessionMapper.selectPage(pageParam,
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getStatus, SESSION_STATUS_WAITING)
                        .orderByAsc(ChatSession::getCreateTime));
        return result.getRecords().stream().map(this::toSessionDTO).collect(Collectors.toList());
    }

    @Override
    public List<ChatSessionDTO> getAgentSessions(Long agentId, Integer status, int page, int size) {
        Page<ChatSession> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getCsAgentId, agentId);
        if (status != null) {
            wrapper.eq(ChatSession::getStatus, status);
        }
        wrapper.orderByDesc(ChatSession::getUpdateTime);

        Page<ChatSession> result = chatSessionMapper.selectPage(pageParam, wrapper);
        return result.getRecords().stream().map(this::toSessionDTO).collect(Collectors.toList());
    }

    @Override
    public ChatSessionDTO getSessionDetailForAgent(Long sessionId, Long agentId) {
        LambdaQueryWrapper<ChatSession> chatSessionLambdaQueryWrapper = new LambdaQueryWrapper<>();
        chatSessionLambdaQueryWrapper.eq(ChatSession::getId, sessionId)
                .eq(ChatSession::getCsAgentId, agentId);
        ChatSession session = chatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(SESSION_NOT_EXIST);
        }

        List<ChatMessage> messages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByAsc(ChatMessage::getCreateTime));

        ChatSessionDTO dto = toSessionDTO(session);
        dto.setMessages(messages.stream().map(this::toMessageDTO).collect(Collectors.toList()));
        return dto;
    }





    private ChatSessionDTO toSessionDTO(ChatSession session) {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
        dto.setSpuId(session.getSpuId());
        dto.setStatus(session.getStatus());
        dto.setCsAgentId(session.getCsAgentId());
        dto.setLastMessage(session.getLastMessage());
        dto.setLastMessageTime(session.getLastMessageTime());
        dto.setCreateTime(session.getCreateTime());
        return dto;
    }

    private ChatMessageDTO toMessageDTO(ChatMessage message) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSessionId(message.getSessionId());
        dto.setSenderType(message.getSenderType());
        dto.setSenderId(message.getSenderId());
        dto.setContent(message.getContent());
        dto.setCreateTime(message.getCreateTime());
        return dto;
    }
}
