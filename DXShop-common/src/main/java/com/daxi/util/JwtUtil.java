package com.daxi.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
@Component
public class JwtUtil {

    // 建议在生产环境中将秘钥和过期时间配置在 application.yml 中
    private static final String SECRET = "your-very-secure-and-long-secret-key-must-be-at-least-256-bits-long";
    private static final long EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 过期时间：7天 (单位:毫秒)

    // 使用强类型的安全算法 (HMAC-SHA256)
    private static final SecureDigestAlgorithm<SecretKey, SecretKey> ALGORITHM = Jwts.SIG.HS256;

    // 私有化构造器，防止实例化
    private JwtUtil() {}

    /**
     * 生成安全密钥
     * 注意：SECRET 的长度必须满足算法要求（HS256至少需要256位，即32字节）
     */
    private static SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param subject   主题（通常为用户ID或用户名）
     * @param claims    自定义声明（载荷信息）
     * @return Token 字符串
     */
    public static String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .subject(subject)                       // 设置主题
                .claims(claims)                         // 设置自定义载荷
                .issuedAt(now)                          // 设置签发时间
                .expiration(expirationDate)             // 设置过期时间
                .signWith(getSigningKey(), ALGORITHM)   // 使用安全密钥和算法签名
                .compact();
    }

    /**
     * 解析 JWT Token
     *
     * @param token Token 字符串
     * @return Jws<Claims> 对象，包含Header、Payload和Signature信息
     * @throws JwtException 当Token无效、过期或签名错误时抛出
     */
    public static Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())            // 验证签名密钥
                .build()
                .parseSignedClaims(token);              // 解析并验证Token
    }

    /**
     * 从 Token 中获取主题（如用户ID）
     */
    public static String getSubject(String token) throws JwtException {
        return parseToken(token).getPayload().getSubject();
    }

    /**
     * 从 Token 中获取自定义声明
     */
    public static Claims getClaims(String token) throws JwtException {
        return parseToken(token).getPayload();
    }

    /**
     * 判断 Token 是否过期
     *
     * @param token Token 字符串
     * @return true=已过期，false=未过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Date expiration = parseToken(token).getPayload().getExpiration();
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            // 如果捕获到过期异常，说明确实已过期
            return true;
        } catch (JwtException e) {
            // 其他异常（如签名错误、格式错误）也视为无效Token
            return true;
        }
    }

    /**
     * 验证 Token 有效性
     *
     * @param token Token 字符串
     * @return true=有效，false=无效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            // 在实际业务中，可以在这里记录日志
            // log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
