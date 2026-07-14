package com.daxi.service.Impl;

import com.daxi.Exception.BusinessException;
import com.daxi.domain.entity.ChatMessage;
import com.daxi.domain.entity.ChatSession;
import com.daxi.domain.entity.CsAgent;
import com.daxi.mapper.ChatMessageMapper;
import com.daxi.mapper.ChatSessionMapper;
import com.daxi.mapper.CsAgentMapper;
import com.daxi.test.MyBatisPlusTestSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ChatServiceImpl 纯单元测试（Mockito 隔离所有 mapper / redis / websocket / server）。
 * 因业务方法内部构造 LambdaQueryWrapper，需先初始化 MyBatis-Plus TableInfo 缓存。
 */
@DisplayName("ChatServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private CsAgentMapper csAgentMapper;
    @Mock
    private ChatSessionMapper chatSessionMapper;
    @Mock
    private ChatMessageMapper chatMessageMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ServerImpl server;
    @InjectMocks
    private ChatServiceImpl chatService;

    @BeforeAll
    static void initMeta() {
        MyBatisPlusTestSupport.initTableInfo(CsAgent.class, ChatSession.class, ChatMessage.class);
    }

    @Nested
    @DisplayName("agentLogin 客服登录")
    class AgentLogin {

        @Test
        @DisplayName("账号密码正确 -> 返回 token 并写入 Redis")
        void success() {
            CsAgent agent = new CsAgent();
            agent.setId(1L);
            agent.setUsername("admin");
            agent.setPasswordHash("ENC");
            agent.setNickname("小溪");
            agent.setAvatarUrl("a.png");
            when(csAgentMapper.selectOne(any())).thenReturn(agent);
            when(passwordEncoder.matches("pwd", "ENC")).thenReturn(true);
            ValueOperations<String, String> vops = mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(vops);

            var dto = chatService.agentLogin("admin", "pwd");

            assertThat(dto).isNotNull();
            assertThat(dto.getAgentId()).isEqualTo(1L);
            assertThat(dto.getToken()).isNotBlank();
        }

        @Test
        @DisplayName("账号不存在 -> 业务异常")
        void notExist() {
            when(csAgentMapper.selectOne(any())).thenReturn(null);
            assertThatThrownBy(() -> chatService.agentLogin("x", "pwd"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("createSession 创建会话")
    class CreateSession {

        @Test
        @DisplayName("无重复会话 -> 新建并返回 DTO")
        void success() {
            when(chatSessionMapper.selectOne(any())).thenReturn(null);

            var dto = chatService.createSession(100L, 5L);

            assertThat(dto).isNotNull();
            assertThat(dto.getUserId()).isEqualTo(100L);
            assertThat(dto.getSpuId()).isEqualTo(5L);
        }

        @Test
        @DisplayName("已存在未关闭会话 -> 业务异常")
        void alreadyExists() {
            ChatSession existing = new ChatSession();
            existing.setId(9L);
            when(chatSessionMapper.selectOne(any())).thenReturn(existing);

            assertThatThrownBy(() -> chatService.createSession(100L, 5L))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
