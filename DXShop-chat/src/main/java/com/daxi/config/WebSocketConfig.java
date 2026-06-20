package com.daxi.config;


import com.daxi.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static com.daxi.key.redis.UserKey.AGENT_LOGIN;
import static com.daxi.limit.ChatLimit.AGENT_TOKEN_PREFIX;
import static com.daxi.limit.ChatLimit.JWT_ROLE_AGENT;
import static com.daxi.limit.UserLimit.LOGIN_TOKEN_PREFIX;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final @NonNull StringRedisTemplate stringRedisTemplate;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token != null && token.startsWith(LOGIN_TOKEN_PREFIX)) {
                        token = token.substring(7);
                        try {
                            String subject = JwtUtil.getSubject(token);
                            Claims claims = JwtUtil.getClaims(token);
                            String role = claims.get("role", String.class);

                            if (JWT_ROLE_AGENT.equals(role)) {
                                String redisToken = stringRedisTemplate.opsForValue()
                                        .get(String.format(AGENT_TOKEN_PREFIX, Long.valueOf(subject)));
                                if (redisToken != null && redisToken.equals(token)) {
                                    accessor.setUser(() -> "AGENT:" + subject);
                                    log.debug("Agent WebSocket authenticated: {}", subject);
                                }
                            } else{
                                String redisToken = stringRedisTemplate.opsForValue()
                                        .get(AGENT_LOGIN.format(Long.valueOf(subject)));
                                if (redisToken != null && redisToken.equals(token)) {
                                    accessor.setUser(() -> "USER:" + subject);
                                    log.debug("User WebSocket authenticated: {}", subject);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("WebSocket auth failed: {}", e.getMessage());
                        }
                    }
                }
                return message;
            }
        });
    }
}
