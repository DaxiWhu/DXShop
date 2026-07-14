package com.daxi.service.Impl;

import com.daxi.Exception.BusinessException;
import com.daxi.domain.ao.LoginRequestAO;
import com.daxi.domain.ao.RegisterRequestAO;
import com.daxi.domain.dto.LoginResponseDTO;
import com.daxi.domain.dto.UserDisplayDTO;
import com.daxi.domain.dto.UserPrivateDTO;
import com.daxi.domain.entity.UserCart;
import com.daxi.domain.entity.UserDisplay;
import com.daxi.domain.entity.UserPrivate;
import com.daxi.domain.entity.UserShopFollow;
import com.daxi.feign.GoodsFeignClient;
import com.daxi.feign.OrderFeignClient;
import com.daxi.mapper.UserCartMapper;
import com.daxi.mapper.UserDisplayMapper;
import com.daxi.mapper.UserGoodsFollowMapper;
import com.daxi.mapper.UserPrivateMapper;
import com.daxi.mapper.UserShopFollowMapper;
import com.daxi.mapper.UserShopPayDetailMapper;
import com.daxi.mapper.UserSpuPayDetailMapper;
import com.daxi.test.MyBatisPlusTestSupport;
import com.daxi.util.RedisUtil;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static com.daxi.limit.UserLimit.USER_FOLLOW;
import static com.daxi.limit.UserLimit.USER_NOT_FOLLOW;
import static com.daxi.limit.UserLimit.USER_STATUS_LOCKED;
import static com.daxi.limit.UserLimit.USER_STATUS_LOGOUT;
import static com.daxi.response.UserResponse.ACCOUNT_CANCELLED;
import static com.daxi.response.UserResponse.ACCOUNT_FROZEN;
import static com.daxi.response.UserResponse.PASSWORD_ERROR;
import static com.daxi.response.UserResponse.PHONE_ALREADY_REGISTERED;
import static com.daxi.response.UserResponse.PHONE_NOT_REGISTERED;
import static com.daxi.response.UserResponse.VERIFY_CODE_ERROR;
import static com.daxi.response.UserResponse.VERIFY_CODE_EXPIRED;
import static com.daxi.response.UserResponse.VERIFY_CODE_SEND_TOO_FREQUENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserServiceImpl 纯单元测试。
 *
 * <p>用 Mockito 隔离全部外部依赖（Mapper / Redis / Feign / PasswordEncoder），
 * 不启动 Spring 上下文、不连任何中间件。只覆盖不依赖
 * {@code new LambdaUpdateChainWrapper} 静态链式构造的纯逻辑分支：
 * 验证码、注册/登录分支、字段脱敏、查询结果映射。
 */
@DisplayName("UserServiceImpl 单元测试")
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserShopFollowMapper userShopFollowMapper;
    @Mock private UserGoodsFollowMapper userGoodsFollowMapper;
    @Mock private UserCartMapper userCartMapper;
    @Mock private UserSpuPayDetailMapper userSpuPayDetailMapper;
    @Mock private UserShopPayDetailMapper userShopPayDetailMapper;
    @Mock private OrderFeignClient orderFeignClient;
    @Mock private GoodsFeignClient goodsFeignClient;
    @Mock private ServiceImpl service;
    @Mock private UserDisplayMapper userDisplayMapper;
    @Mock private UserPrivateMapper userPrivateMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RedisUtil redisUtil;
    @Mock private StringRedisTemplate stringRedisTemplate;

    @InjectMocks private UserServiceImpl userService;

    /**
     * 纯单测不启动 Spring，需手动为涉及到的实体初始化 MyBatis-Plus
     * TableInfo 缓存，否则业务里构造 LambdaQueryWrapper 会抛“找不到 lambda 缓存”。
     */
    @BeforeAll
    static void initMyBatisPlusMeta() {
        MyBatisPlusTestSupport.initTableInfo(
                UserPrivate.class, UserDisplay.class, UserCart.class, UserShopFollow.class);
    }

    // ==================== getVerifyCode ====================

    @Nested
    @DisplayName("getVerifyCode 获取验证码")
    class GetVerifyCode {

        @Test
        @DisplayName("60秒内重复获取 -> 抛发送过于频繁")
        void limited_throws() {
            when(redisUtil.isVerifyCodeLimited("13800000000")).thenReturn(true);

            assertThatThrownBy(() -> userService.getVerifyCode("13800000000"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(VERIFY_CODE_SEND_TOO_FREQUENT);

            verify(redisUtil, never()).setVerifyCode(anyString(), anyString());
        }

        @Test
        @DisplayName("正常获取 -> 返回6位纯数字验证码并写入Redis")
        void normal_returnsSixDigitCode() {
            when(redisUtil.isVerifyCodeLimited("13800000000")).thenReturn(false);

            String code = userService.getVerifyCode("13800000000");

            assertThat(code).hasSize(6).containsOnlyDigits();
            verify(redisUtil).setVerifyCode("13800000000", code);
        }
    }

    // ==================== register ====================

    @Nested
    @DisplayName("register 注册")
    class Register {

        private RegisterRequestAO req(String verifyCode) {
            RegisterRequestAO ao = new RegisterRequestAO();
            ao.setPhone("13812345678");
            ao.setPassword("password123");
            ao.setVerifyCode(verifyCode);
            return ao;
        }

        @Test
        @DisplayName("验证码已过期(Redis为null) -> 抛验证码已过期")
        void codeExpired_throws() {
            when(redisUtil.getVerifyCode("13812345678")).thenReturn(null);

            assertThatThrownBy(() -> userService.register(req("123456")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(VERIFY_CODE_EXPIRED);

            verify(userPrivateMapper, never()).insert(any(UserPrivate.class));
        }

        @Test
        @DisplayName("验证码不匹配 -> 抛验证码错误")
        void codeMismatch_throws() {
            when(redisUtil.getVerifyCode("13812345678")).thenReturn("999999");

            assertThatThrownBy(() -> userService.register(req("123456")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(VERIFY_CODE_ERROR);

            verify(userPrivateMapper, never()).insert(any(UserPrivate.class));
        }

        @Test
        @DisplayName("手机号已注册 -> 抛该手机号已注册")
        void phoneExists_throws() {
            when(redisUtil.getVerifyCode("13812345678")).thenReturn("123456");
            when(userPrivateMapper.exists(any())).thenReturn(true);

            assertThatThrownBy(() -> userService.register(req("123456")))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(PHONE_ALREADY_REGISTERED);

            verify(userPrivateMapper, never()).insert(any(UserPrivate.class));
        }

        @Test
        @DisplayName("注册成功 -> 删验证码、加密密码、写入两张表")
        void success_insertsBothTables() {
            when(redisUtil.getVerifyCode("13812345678")).thenReturn("123456");
            when(userPrivateMapper.exists(any())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("ENCODED");

            userService.register(req("123456"));

            verify(redisUtil).deleteVerifyCode("13812345678");
            verify(passwordEncoder).encode("password123");
            verify(userPrivateMapper, times(1)).insert(any(UserPrivate.class));
            verify(userDisplayMapper, times(1)).insert(any(UserDisplay.class));
        }
    }

    // ==================== login ====================

    @Nested
    @DisplayName("login 登录")
    class Login {

        private LoginRequestAO req() {
            LoginRequestAO ao = new LoginRequestAO();
            ao.setPhone("13812345678");
            ao.setPassword("password123");
            return ao;
        }

        @Test
        @DisplayName("手机号未注册 -> 抛该手机号未注册")
        void phoneNotRegistered_throws() {
            when(userPrivateMapper.selectOne(any())).thenReturn(null);

            assertThatThrownBy(() -> userService.login(req()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(PHONE_NOT_REGISTERED);
        }

        @Test
        @DisplayName("密码错误 -> 抛手机号或密码错误")
        void wrongPassword_throws() {
            UserPrivate up = new UserPrivate();
            up.setUserId(1001L);
            up.setPasswordHash("ENCODED");
            when(userPrivateMapper.selectOne(any())).thenReturn(up);
            when(passwordEncoder.matches("password123", "ENCODED")).thenReturn(false);

            assertThatThrownBy(() -> userService.login(req()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(PASSWORD_ERROR);
        }

        @Test
        @DisplayName("账号被冻结 -> 抛账号已被冻结")
        void accountFrozen_throws() {
            UserPrivate up = new UserPrivate();
            up.setUserId(1001L);
            up.setPasswordHash("ENCODED");
            when(userPrivateMapper.selectOne(any())).thenReturn(up);
            when(passwordEncoder.matches("password123", "ENCODED")).thenReturn(true);

            UserDisplay ud = new UserDisplay();
            ud.setAccountStatus(USER_STATUS_LOCKED);
            when(userDisplayMapper.selectOne(any())).thenReturn(ud);

            assertThatThrownBy(() -> userService.login(req()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ACCOUNT_FROZEN);
        }

        @Test
        @DisplayName("账号已注销 -> 抛账号已注销")
        void accountCancelled_throws() {
            UserPrivate up = new UserPrivate();
            up.setUserId(1001L);
            up.setPasswordHash("ENCODED");
            when(userPrivateMapper.selectOne(any())).thenReturn(up);
            when(passwordEncoder.matches("password123", "ENCODED")).thenReturn(true);

            UserDisplay ud = new UserDisplay();
            ud.setAccountStatus(USER_STATUS_LOGOUT);
            when(userDisplayMapper.selectOne(any())).thenReturn(ud);

            assertThatThrownBy(() -> userService.login(req()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ACCOUNT_CANCELLED);
        }

        @Test
        @DisplayName("登录成功 -> 返回带Bearer前缀的Token并写入Redis")
        void success_returnsTokenWithPrefix() {
            UserPrivate up = new UserPrivate();
            up.setUserId(1001L);
            up.setPasswordHash("ENCODED");
            when(userPrivateMapper.selectOne(any())).thenReturn(up);
            when(passwordEncoder.matches("password123", "ENCODED")).thenReturn(true);
            // accountStatus 查询返回 null -> 跳过状态校验
            when(userDisplayMapper.selectOne(any())).thenReturn(null);

            @SuppressWarnings("unchecked")
            ValueOperations<String, String> valueOps = org.mockito.Mockito.mock(ValueOperations.class);
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

            LoginResponseDTO dto = userService.login(req());

            assertThat(dto).isNotNull();
            assertThat(dto.getUserId()).isEqualTo(1001L);
            assertThat(dto.getToken()).startsWith("Bearer ");
            verify(valueOps).set(anyString(), anyString());
        }
    }

    // ==================== 脱敏 getPrivateInfo ====================

    @Nested
    @DisplayName("getPrivateInfo 隐私信息脱敏")
    class GetPrivateInfo {

        @Test
        @DisplayName("手机号/邮箱正确脱敏")
        void masksPhoneAndEmail() {
            UserPrivate up = new UserPrivate();
            up.setUserId(1001L);
            up.setPhone("13812345678");
            up.setEmail("zhangsan@example.com");
            when(userPrivateMapper.selectOne(any())).thenReturn(up);

            UserPrivateDTO dto = userService.getPrivateInfo(1001L);

            assertThat(dto).isNotNull();
            assertThat(dto.getPhone()).isEqualTo("138****5678");
            assertThat(dto.getEmail()).isEqualTo("zh***@example.com");
        }

        @Test
        @DisplayName("用户不存在 -> 返回null")
        void notFound_returnsNull() {
            when(userPrivateMapper.selectOne(any())).thenReturn(null);

            assertThat(userService.getPrivateInfo(9999L)).isNull();
        }
    }

    // ==================== getDisplayInfo ====================

    @Nested
    @DisplayName("getDisplayInfo 展示信息")
    class GetDisplayInfo {

        @Test
        @DisplayName("存在 -> 正确映射为DTO")
        void present_mapsToDto() {
            UserDisplay ud = new UserDisplay();
            ud.setUserId(1001L);
            ud.setNickname("大溪用户");
            ud.setGender(1);
            when(userDisplayMapper.selectOne(any())).thenReturn(ud);

            UserDisplayDTO dto = userService.getDisplayInfo(1001L);

            assertThat(dto).isNotNull();
            assertThat(dto.getUserId()).isEqualTo(1001L);
            assertThat(dto.getNickname()).isEqualTo("大溪用户");
            assertThat(dto.getGender()).isEqualTo(1);
        }

        @Test
        @DisplayName("不存在 -> 返回null")
        void notFound_returnsNull() {
            when(userDisplayMapper.selectOne(any())).thenReturn(null);

            assertThat(userService.getDisplayInfo(9999L)).isNull();
        }
    }

    // ==================== getMyCart ====================

    @Nested
    @DisplayName("getMyCart 购物车")
    class GetMyCart {

        @Test
        @DisplayName("无数据(null) -> 返回空列表(非null)")
        void empty_returnsEmptyList() {
            when(userCartMapper.selectList(any())).thenReturn(null);

            List<UserCart> carts = userService.getMyCart(1001L);

            assertThat(carts).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("有数据 -> 原样返回")
        void hasData_returnsList() {
            UserCart c = new UserCart();
            when(userCartMapper.selectList(any())).thenReturn(List.of(c));

            assertThat(userService.getMyCart(1001L)).hasSize(1);
        }
    }

    // ==================== checkIsFollowShop ====================

    @Nested
    @DisplayName("checkIsFollowShop 是否关注店铺")
    class CheckIsFollowShop {

        @Test
        @DisplayName("无关注记录 -> 返回未关注")
        void notFollowed_returnsNotFollow() {
            when(userShopFollowMapper.selectOne(any())).thenReturn(null);

            assertThat(userService.checkIsFollowShop(1001L, 66L)).isEqualTo(USER_NOT_FOLLOW);
        }

        @Test
        @DisplayName("有关注记录 -> 返回已关注")
        void followed_returnsFollow() {
            when(userShopFollowMapper.selectOne(any())).thenReturn(new UserShopFollow());

            assertThat(userService.checkIsFollowShop(1001L, 66L)).isEqualTo(USER_FOLLOW);
        }
    }
}
