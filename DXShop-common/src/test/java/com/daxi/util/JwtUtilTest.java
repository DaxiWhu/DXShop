package com.daxi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtUtil 纯单元测试：不依赖 Spring 上下文、不连任何中间件。
 * 覆盖 生成/解析/校验/过期/篡改 等核心路径与边界。
 */
@DisplayName("JwtUtil 单元测试")
class JwtUtilTest {

    @Test
    @DisplayName("生成的 Token 能解析回原始 subject")
    void generateThenGetSubject_roundTrips() {
        String token = JwtUtil.generateToken("10086", new HashMap<>());

        assertThat(JwtUtil.getSubject(token)).isEqualTo("10086");
    }

    @Test
    @DisplayName("自定义 claims 能被正确读取")
    void generateWithClaims_claimsReadable() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USER");
        claims.put("shopId", 66);

        String token = JwtUtil.generateToken("10086", claims);

        Claims parsed = JwtUtil.getClaims(token);
        assertThat(parsed.getSubject()).isEqualTo("10086");
        assertThat(parsed.get("role")).isEqualTo("USER");
        assertThat(parsed.get("shopId")).isEqualTo(66);
    }

    @Test
    @DisplayName("有效 Token 校验通过、未过期")
    void validToken_isValidAndNotExpired() {
        String token = JwtUtil.generateToken("1", new HashMap<>());

        assertThat(JwtUtil.validateToken(token)).isTrue();
        assertThat(JwtUtil.isTokenExpired(token)).isFalse();
    }

    @ParameterizedTest
    @DisplayName("非法/垃圾 Token 校验一律失败")
    @ValueSource(strings = {"garbage", "a.b.c", "", "   ", "xxxxx.yyyyy.zzzzz"})
    void invalidToken_validateReturnsFalse(String bad) {
        assertThat(JwtUtil.validateToken(bad)).isFalse();
        // 无效 Token 视为已过期
        assertThat(JwtUtil.isTokenExpired(bad)).isTrue();
    }

    @Test
    @DisplayName("解析非法 Token 抛 JwtException")
    void parseInvalidToken_throws() {
        assertThatThrownBy(() -> JwtUtil.parseToken("not-a-real-token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("被篡改的 Token 签名校验失败")
    void tamperedToken_isRejected() {
        String token = JwtUtil.generateToken("1", new HashMap<>());
        // 篡改最后一位（签名段），破坏签名
        String tampered = token.substring(0, token.length() - 2)
                + (token.endsWith("A") ? "B" : "A");

        assertThat(JwtUtil.validateToken(tampered)).isFalse();
    }
}
