package com.daxi.interceptor;

import com.daxi.Exception.BusinessException;
import com.daxi.annotation.AnonymousAccess;
import com.daxi.annotation.InternalApi;
import com.daxi.util.JwtUtil;
import com.daxi.util.UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.daxi.key.redis.UserKey.LOGIN;
import static com.daxi.key.redis.UserKey.SHOP_LOGIN;
import static com.daxi.limit.UserLimit.LOGIN_TOKEN_PREFIX;
import static com.daxi.response.UserResponse.NOT_LOGIN;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private @NonNull final StringRedisTemplate stringRedisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        // 放行 OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        //判断当前处理的方法或类上是否有 @AnonymousAccess 注解
        if (handler instanceof HandlerMethod handlerMethod) {
            // 检查方法上是否有注解
            AnonymousAccess methodAnnotation = handlerMethod.getMethodAnnotation(AnonymousAccess.class);

            InternalApi internalApi = handlerMethod.getMethodAnnotation(InternalApi.class);


            if (methodAnnotation != null || internalApi != null) {
                return true; // 有注解，直接放行
            }
        }

        // 3. 以下才是校验 Token 的逻辑（不在白名单的请求才会走到这里）
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith(LOGIN_TOKEN_PREFIX)) {
            token = token.substring(7);
            try {
                String userIdStr = JwtUtil.getSubject(token);
                if(userIdStr== null){
                    response.setStatus(401);
                    response.getWriter().write("Unauthorized: Invalid or expired token");
                    return false;
                }
                Long userId = Long.valueOf(userIdStr);
                String redisToken = stringRedisTemplate.opsForValue().get(LOGIN.format(userId));
                if (redisToken == null || !redisToken.equals(token)) {
                    response.setStatus(401);
                    response.getWriter().write("Unauthorized: Invalid or expired token");
                    return false;
                }
                // 存入 ThreadLocal
                UserUtil.setLocalUserId(userId);
                //再看一下有没有shopId
                String shopToken= request.getHeader("shop");
                if(shopToken != null && shopToken.startsWith(LOGIN_TOKEN_PREFIX)){
                    shopToken = shopToken.substring(7);
                    String shopIdStr = null;

                    if(!JwtUtil.isTokenExpired(shopToken)){
                        shopIdStr = JwtUtil.getSubject(shopToken);
                    }

                    if(shopIdStr != null){
                        String shopTokenInRedis = stringRedisTemplate.opsForValue().get(SHOP_LOGIN.format(userId));
                        if(shopTokenInRedis != null && shopTokenInRedis.equals(shopToken)) {
                            UserUtil.setLocalShopId(Long.valueOf(shopIdStr));
                        }
                    }
                }
                return true;
            } catch (Exception e) {
                throw new BusinessException(NOT_LOGIN);
            }
        }

        throw new BusinessException(NOT_LOGIN);
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler, Exception ex) {
        // 清理 ThreadLocal
        UserUtil.remove();

    }

}