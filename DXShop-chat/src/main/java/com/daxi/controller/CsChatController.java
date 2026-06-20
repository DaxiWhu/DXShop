package com.daxi.controller;

import com.daxi.annotation.AnonymousAccess;
import com.daxi.domain.ao.AgentLoginAO;
import com.daxi.domain.ao.GetAgentSessionsAO;
import com.daxi.domain.ao.GetPendingSessionsAO;
import com.daxi.domain.ao.SendMessageAO;
import com.daxi.domain.dto.AgentLoginDTO;
import com.daxi.domain.dto.ChatMessageDTO;
import com.daxi.domain.dto.ChatSessionDTO;
import com.daxi.result.Result;
import com.daxi.service.ChatService;
import com.daxi.util.UserUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.daxi.limit.GoodsLimit.MIN_ID_SCALE;
import static com.daxi.response.ChatResponse.AGENT_LOGIN_SUCCESS;
import static com.daxi.response.ChatResponse.AGENT_NOT_LOGIN;

@Slf4j
@RestController
@RequestMapping("/cs")
@RequiredArgsConstructor
public class CsChatController {

    private final @NonNull ChatService chatService;
    //客服登录1
    @AnonymousAccess
    @PostMapping("/agent/login")
    public Result<AgentLoginDTO> login(@Valid @RequestBody AgentLoginAO ao) {
        AgentLoginDTO dto = chatService.agentLogin(ao.getUsername(), ao.getPassword());
        return Result.success(AGENT_LOGIN_SUCCESS, dto);
    }
    //获取正在等待处理的会话
    @AnonymousAccess
    @GetMapping("/session/pending")
    public Result<List<ChatSessionDTO>> getPendingSessions(
            @Valid GetPendingSessionsAO ao) {
        Long agentId = UserUtil.getAgentId();
        if (agentId == null) {
            return Result.fail(AGENT_NOT_LOGIN);
        }
        return Result.success(chatService.getPendingSessions(ao.getPage(), ao.getSize()));
    }
    //获取客服会话列表
    @AnonymousAccess
    @GetMapping("/session")
    public Result<List<ChatSessionDTO>> getAgentSessions(
        @Valid @RequestBody GetAgentSessionsAO ao) {
        Long agentId = UserUtil.getAgentId();

        if (agentId == null) {
            return Result.fail(AGENT_NOT_LOGIN);
        }
        return Result.success(chatService.getAgentSessions(
                agentId,
                ao.getStatus(),
                ao.getPage(),
                ao.getSize()));
    }
    //客服接管会话
    @AnonymousAccess
    @PutMapping("/session/{sessionId}/take")
    public Result<Void> takeSession(
            @Valid @Min (value = MIN_ID_SCALE)@PathVariable Long sessionId) {
        Long agentId = UserUtil.getAgentId();
        if (agentId == null) {
            return Result.fail(AGENT_NOT_LOGIN);
        }
        chatService.takeSession(sessionId, agentId);
        return Result.success();
    }
    //客服发送消息
    @AnonymousAccess
    @PostMapping("/session/{sessionId}/message")
    public Result<ChatMessageDTO> sendAgentMessage(
            @Valid @Min (value = MIN_ID_SCALE) @PathVariable Long sessionId,
            @Valid @RequestBody SendMessageAO ao) {
        Long agentId = UserUtil.getAgentId();
        if (agentId == null) {
            return Result.fail(AGENT_NOT_LOGIN);
        }
        return Result.success(chatService.sendAgentMessage(sessionId, agentId, ao.getContent()));
    }
    //客服关闭会话
    @AnonymousAccess
    @PutMapping("/session/{sessionId}/close")
    public Result<Void> closeSession(
            @Valid @Min (value = MIN_ID_SCALE) @PathVariable Long sessionId) {
        Long agentId = UserUtil.getAgentId();
        if (agentId == null) {
            return Result.fail(AGENT_NOT_LOGIN);
        }
        chatService.closeSession(sessionId,agentId);
        return Result.success();
    }


}
