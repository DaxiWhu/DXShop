package com.daxi.controller;

import com.daxi.domain.ao.SendMessageAO;
import com.daxi.domain.dto.ChatMessageDTO;
import com.daxi.domain.dto.ChatSessionDTO;
import com.daxi.result.Result;
import com.daxi.service.ChatService;
import com.daxi.util.UserUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.daxi.limit.GoodsLimit.MIN_ID_SCALE;
import static com.daxi.response.UserResponse.NOT_LOGIN;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatInternalController {

    private final @NonNull ChatService chatService;
    //创建会话
    @PostMapping("/session")
    public Result<ChatSessionDTO> createSession(
            @Min(value=MIN_ID_SCALE) @RequestParam Long spuId) {
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(chatService.createSession(userId, spuId));
    }
    //获取用户当前存在的会话

    @GetMapping("/session")
    public Result<List<ChatSessionDTO>> getSessions() {
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(chatService.getUserSessions(userId));
    }
    //用户获取会话详情
    @GetMapping("/session/{sessionId}/user")
    public Result<ChatSessionDTO> getSessionDetailForUser(
            @Valid @Min (value = MIN_ID_SCALE)@PathVariable Long sessionId) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(chatService.getSessionDetailForUser(sessionId, userId));
    }
    //用户发送消息
    @PostMapping("/session/{sessionId}/message/user")
    public Result<ChatMessageDTO> sendUserMessage(
            @Valid @Min (value = MIN_ID_SCALE)@PathVariable Long sessionId,
            @Valid @RequestBody SendMessageAO ao) {
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        return Result.success(chatService.sendUserMessage(sessionId, userId, ao.getContent()));
    }

    //用户关闭会话

    @PutMapping("/session/{sessionId}/close")
    public Result<Void> closeSession(
            @Valid @Min (value = MIN_ID_SCALE)@PathVariable Long sessionId) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            return Result.fail(NOT_LOGIN);
        }
        chatService.closeSession(sessionId, userId);
        return Result.success();
    }
}
