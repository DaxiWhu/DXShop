package com.daxi.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.daxi.Exception.BusinessException;
import com.daxi.domain.ao.LoginRequestAO;
import com.daxi.domain.ao.RegisterRequestAO;
import com.daxi.domain.ao.UserDisplayAO;
import com.daxi.domain.ao.UserFollowSkuAO;
import com.daxi.domain.ao.UserPrivateAO;
import com.daxi.domain.dto.LoginResponseDTO;
import com.daxi.domain.dto.SendCommentDTO;
import com.daxi.domain.dto.UserCartSkuDTO;
import com.daxi.domain.dto.UserDisplayDTO;
import com.daxi.domain.dto.UserFavoritesShopDTO;
import com.daxi.domain.dto.UserFavoritesSpuDTO;
import com.daxi.domain.dto.UserOrderDTO;
import com.daxi.domain.dto.UserPrivateDTO;
import com.daxi.domain.entity.UserCart;
import com.daxi.domain.entity.UserDisplay;
import com.daxi.domain.entity.UserGoodsFollow;
import com.daxi.domain.entity.UserPrivate;
import com.daxi.domain.entity.UserShopFollow;
import com.daxi.domain.entity.UserShopPayDetail;
import com.daxi.domain.entity.UserSpuPayDetail;
import com.daxi.feign.GoodsFeignClient;
import com.daxi.feign.OrderFeignClient;
import com.daxi.mapper.UserCartMapper;
import com.daxi.mapper.UserDisplayMapper;
import com.daxi.mapper.UserGoodsFollowMapper;
import com.daxi.mapper.UserPrivateMapper;
import com.daxi.mapper.UserShopFollowMapper;
import com.daxi.mapper.UserShopPayDetailMapper;
import com.daxi.mapper.UserSpuPayDetailMapper;
import com.daxi.service.UserService;
import com.daxi.util.JwtUtil;
import com.daxi.util.PriceUtil;
import com.daxi.util.RedisUtil;
import com.daxi.util.UserUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.daxi.key.redis.UserKey.LOGIN;
import static com.daxi.limit.OrderLimit.ORDER_EFFECTIVE;
import static com.daxi.limit.UserLimit.AUTO_USER_NAME_PREFIX;
import static com.daxi.limit.UserLimit.LOGIN_TOKEN_PREFIX;
import static com.daxi.limit.UserLimit.SKU_EFFECTIVE;
import static com.daxi.limit.UserLimit.USER_CART_NOT_CHECKED;
import static com.daxi.limit.UserLimit.USER_FIRST_BUY;
import static com.daxi.limit.UserLimit.USER_FOLLOW;
import static com.daxi.limit.UserLimit.USER_LOGIN_EFFECTIVE_DAY;
import static com.daxi.limit.UserLimit.USER_NOT_FOLLOW;
import static com.daxi.limit.UserLimit.USER_NOT_REAL_NAME;
import static com.daxi.limit.UserLimit.USER_RETURN_BUY;
import static com.daxi.limit.UserLimit.USER_SEX_UNKNOWN;
import static com.daxi.limit.UserLimit.USER_STATUS_LOCKED;
import static com.daxi.limit.UserLimit.USER_STATUS_LOGOUT;
import static com.daxi.limit.UserLimit.USER_STATUS_NORMAL;
import static com.daxi.limit.UserLimit.VERIFY_CODE_LENGTH;
import static com.daxi.response.OrderResponse.ORDER_NOT_EXIST;
import static com.daxi.response.OrderResponse.PARAM_ERROR;
import static com.daxi.response.UserResponse.ACCOUNT_CANCELLED;
import static com.daxi.response.UserResponse.ACCOUNT_FROZEN;
import static com.daxi.response.UserResponse.PASSWORD_ERROR;
import static com.daxi.response.UserResponse.PHONE_ALREADY_REGISTERED;
import static com.daxi.response.UserResponse.PHONE_NOT_REGISTERED;
import static com.daxi.response.UserResponse.VERIFY_CODE_ERROR;
import static com.daxi.response.UserResponse.VERIFY_CODE_EXPIRED;
import static com.daxi.response.UserResponse.VERIFY_CODE_SEND_TOO_FREQUENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private @NonNull final UserShopFollowMapper userShopFollowMapper;
    private @NonNull final UserGoodsFollowMapper  userGoodsFollowMapper;
    private @NonNull final UserCartMapper userCartMapper;
    private @NonNull final UserSpuPayDetailMapper userSpuPayDetailMapper;
    private @NonNull final UserShopPayDetailMapper userShopPayDetailMapper;
    private @NonNull final OrderFeignClient orderFeignClient;
    private @NonNull final GoodsFeignClient goodsFeignClient;
    private @NonNull final ServiceImpl service;
    private @NonNull final UserDisplayMapper userDisplayMapper;
    private @NonNull final UserPrivateMapper userPrivateMapper;
    private @NonNull final PasswordEncoder passwordEncoder;
    private @NonNull final RedisUtil redisUtil;
    private @NonNull final StringRedisTemplate stringRedisTemplate;

    @Override
    public String getVerifyCode(String phone) {
        // 1. 检查发送间隔（60秒防刷）
        if (redisUtil.isVerifyCodeLimited(phone)) {
            throw new BusinessException(VERIFY_CODE_SEND_TOO_FREQUENT);
        }

        // 2. 生成6位随机验证码（行业标准：纯数字，首位不为0）
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int min = (int) Math.pow(10, VERIFY_CODE_LENGTH - 1);
        int max = (int) Math.pow(10, VERIFY_CODE_LENGTH);
        String code = String.valueOf(random.nextInt(min, max));

        // 3. 存入 Redis，过期时间120秒，同时设置60秒发送间隔
        redisUtil.setVerifyCode(phone, code);

        return code;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequestAO registerRequest) {
        String phone = registerRequest.getPhone();

        // 1. 校验验证码
        String cachedCode = redisUtil.getVerifyCode(phone);
        if (cachedCode == null) {
            throw new BusinessException(VERIFY_CODE_EXPIRED);
        }
        if (!cachedCode.equals(registerRequest.getVerifyCode())) {
            throw new BusinessException(VERIFY_CODE_ERROR);
        }

        // 2. 检查手机号是否已注册
        boolean exists = userPrivateMapper.exists(
                new LambdaQueryWrapper<UserPrivate>()
                        .eq(UserPrivate::getPhone, phone)
        );
        if (exists) {
            throw new BusinessException(PHONE_ALREADY_REGISTERED);
        }

        // 3. 验证码校验通过，删除验证码（一次性使用，防止重复）
        redisUtil.deleteVerifyCode(phone);

        // 4. 生成用户ID（使用雪花算法）
        Long userId = IdWorker.getId();

        // 5. 密码加密
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // 6. 插入 user_private 表
        UserPrivate userPrivate = new UserPrivate();
        userPrivate.setUserId(userId);
        userPrivate.setPhone(phone);
        userPrivate.setPasswordHash(encodedPassword);
        userPrivate.setLastLoginTime(LocalDateTime.now());
        userPrivateMapper.insert(userPrivate);

        // 7. 插入 user_display 表（默认昵称为"用户" + 手机号后4位）
        UserDisplay userDisplay = new UserDisplay();
        userDisplay.setUserId(userId);
        userDisplay.setNickname(AUTO_USER_NAME_PREFIX + phone.substring(phone.length() - 4));
        userDisplay.setGender(USER_SEX_UNKNOWN); // 0-未知
        userDisplay.setAccountStatus(USER_STATUS_NORMAL); // 1-正常
        userDisplay.setIsRealName(USER_NOT_REAL_NAME); // 0-未实名
        userDisplay.setRegisterTime(LocalDateTime.now());
        userDisplayMapper.insert(userDisplay);

    }

    @Override
    public LoginResponseDTO login(LoginRequestAO loginRequest) {
        // 1. 根据手机号查询用户隐私信息
        LambdaQueryWrapper<UserPrivate> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPrivate::getPhone, loginRequest.getPhone())
                .select(UserPrivate::getUserId, UserPrivate::getPasswordHash);
        UserPrivate userPrivate = userPrivateMapper.selectOne(queryWrapper);

        // 2. 判断用户是否存在
        if (userPrivate == null) {
            throw new BusinessException(PHONE_NOT_REGISTERED);
        }

        // 3. 校验密码（使用盐值加密后比对）
        if (!passwordEncoder.matches(loginRequest.getPassword(), userPrivate.getPasswordHash())) {
            throw new RuntimeException(PASSWORD_ERROR);
        }

        // 4. 查询账号状态
        UserDisplay userDisplay = userDisplayMapper.selectOne(
                new LambdaQueryWrapper<UserDisplay>()
                        .eq(UserDisplay::getUserId, userPrivate.getUserId())
                        .select(UserDisplay::getAccountStatus));
        if (userDisplay != null) {
            if (userDisplay.getAccountStatus() == USER_STATUS_LOCKED) {
                throw new BusinessException(ACCOUNT_FROZEN);
            }
            if (userDisplay.getAccountStatus() == USER_STATUS_LOGOUT) {
                throw new BusinessException(ACCOUNT_CANCELLED);
            }
        }

        // 5. 生成 JWT Token
        Long userId = userPrivate.getUserId();
        String token = JwtUtil.generateToken(String.valueOf(userId), null);
        stringRedisTemplate.opsForValue().set(LOGIN.format(userId), token);
        stringRedisTemplate.expire(LOGIN.format(userId), USER_LOGIN_EFFECTIVE_DAY, TimeUnit.DAYS);
        // 6. 更新最后登录时间（异步或同步均可）
        new LambdaUpdateChainWrapper<>(userPrivateMapper)
                .eq(UserPrivate::getUserId, userId)
                .set(UserPrivate::getLastLoginTime, LocalDateTime.now())
                .update();

        // 7. 构建返回结果
        return LoginResponseDTO.builder()
                .userId(userId)
                .token(LOGIN_TOKEN_PREFIX+token)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followShop(Long shopId, Long userId) {
        // 1. 查询当前关注状态
        LambdaQueryWrapper<UserShopFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserShopFollow::getUserId, userId)
                .eq(UserShopFollow::getShopId, shopId)
                .select(UserShopFollow::getId, UserShopFollow::getFollowStatus);
        
        UserShopFollow existingFollow = userShopFollowMapper.selectOne(queryWrapper);
        
        if (existingFollow == null) {
            // 2. 不存在记录，新增关注
            UserShopFollow newFollow = new UserShopFollow();
            newFollow.setUserId(userId);
            newFollow.setShopId(shopId);
            newFollow.setFollowStatus(USER_FOLLOW);
            userShopFollowMapper.insert(newFollow);
        } else {
            // 3. 存在记录，切换关注状态
            Integer newStatus = existingFollow.getFollowStatus() == USER_FOLLOW ? USER_NOT_FOLLOW : USER_FOLLOW;

            new LambdaUpdateChainWrapper<>(userShopFollowMapper)
                .eq(UserShopFollow::getId, existingFollow.getId())
                .set(UserShopFollow::getFollowStatus, newStatus)
                .update();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followGoods(Long spuId, Long userId) {
        // 1. 查询当前关注状态
        LambdaQueryWrapper<UserGoodsFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserGoodsFollow::getUserId, userId)
                .eq(UserGoodsFollow::getSpuId, spuId)
                .select(UserGoodsFollow::getId,UserGoodsFollow::getFollowStatus);
        
        UserGoodsFollow existingFollow = userGoodsFollowMapper.selectOne(queryWrapper);
        
        if (existingFollow == null) {
            // 2. 不存在记录，新增关注
            UserGoodsFollow newFollow = new UserGoodsFollow();
            newFollow.setUserId(userId);
            newFollow.setSpuId(spuId);
            newFollow.setFollowStatus(USER_FOLLOW);
            userGoodsFollowMapper.insert(newFollow);
        } else {
            // 3. 存在记录，切换关注状态
            Integer newStatus = existingFollow.getFollowStatus() == USER_FOLLOW ? USER_NOT_FOLLOW : USER_FOLLOW;
            new LambdaUpdateChainWrapper<>(userGoodsFollowMapper)
                .eq(UserGoodsFollow::getId, existingFollow.getId())
                .set(UserGoodsFollow::getFollowStatus, newStatus)
                .update();
        }
    }

    @Override
    @GlobalTransactional
    public void addSkuToShoppingList(UserFollowSkuAO ao, Long userId) {
        // 1. 查询购物车中是否已存在该SKU
        LambdaQueryWrapper<UserCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserCart::getUserId, userId)
                .eq(UserCart::getSkuId, ao.getSkuId())
                .select(UserCart::getCartId);

        UserCart existingCart = userCartMapper.selectOne(queryWrapper);

        if (existingCart == null) {
            // 2. 不存在记录，需要查询SKU信息后新增到购物车
            UserCartSkuDTO skuInfo = goodsFeignClient.getSkuForUserCartById(ao.getSkuId(), ao.getBuyNum());
            if(skuInfo==null){
                throw new BusinessException(PARAM_ERROR);
            }
            UserCart newCart = new UserCart();
            newCart.setUserId(userId);
            newCart.setSkuId(ao.getSkuId());
            newCart.setSpuId(skuInfo.getSpuId());
            newCart.setBuyNum(ao.getBuyNum());
            newCart.setChecked(USER_CART_NOT_CHECKED); // 默认不勾选
            newCart.setStatus(SKU_EFFECTIVE);  // 默认正常状态
            newCart.setPrice(skuInfo.getPrice());
            newCart.setTitle(skuInfo.getTitle());
            newCart.setMainImg(skuInfo.getMainImg());
            newCart.setSkuSpec(skuInfo.getSkuSpec());
            userCartMapper.insert(newCart);
        } else {
            // 3. 存在记录，累加购买数量
            new LambdaUpdateChainWrapper<>(userCartMapper)
                .eq(UserCart::getCartId, existingCart.getCartId())
                .setSql("buy_num = buy_num + " + ao.getBuyNum())
                .update();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeCheckedSkuInShoppingList(Long userId, List<Long> cartIds) {
        // 1. 参数校验
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }
        
        // 2. 切换勾选状态：已勾选→未勾选，未勾选→已勾选
        new LambdaUpdateChainWrapper<>(userCartMapper)
                .eq(UserCart::getUserId, userId)
                .in(UserCart::getCartId, cartIds)
                .setSql("checked = CASE WHEN checked = 1 THEN 0 ELSE 1 END")
                .update();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserCart> getMyCart(Long userId) {
        List<UserCart> carts = userCartMapper.selectList(
                new LambdaQueryWrapper<>(UserCart.class)
                .eq(UserCart::getUserId, userId)
                .orderByDesc(UserCart::getUpdateTime));
        if(carts==null|| CollUtil.isEmpty(carts)){
            return Collections.emptyList();
        }
        return carts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserFavoritesShopDTO> getMyFavoritesShops(Long userId) {
        List<UserFavoritesShopDTO> myFavoritesShops = userShopFollowMapper.getMyFavoritesShops(userId);
        if(myFavoritesShops==null|| CollUtil.isEmpty(myFavoritesShops)){
            return Collections.emptyList();
        }

        return myFavoritesShops;
    }

    @Override
    @Transactional
    public List<UserFavoritesSpuDTO> getMyFavoritesSpus(Long userId) {
        List<UserFavoritesSpuDTO> myFavoritesSpus = userGoodsFollowMapper.getMyFavoriteGoods(userId);
        if(myFavoritesSpus==null|| CollUtil.isEmpty(myFavoritesSpus)){
            return Collections.emptyList();
        }
        myFavoritesSpus.forEach(o->{
            o.setPrice(PriceUtil.fenToYuan(o.getPrice()));
        });

        return myFavoritesSpus;
    }
    @Override
    @GlobalTransactional
    public void receiveOrder(Long orderId) {
        Long userId= UserUtil.getLocalUserId();
        UserOrderDTO receive = orderFeignClient.getOrderForReceiveOrderAndOkStatus(orderId, userId);
        if(receive==null){
            throw new BusinessException(ORDER_NOT_EXIST);
        }

        String dt = receive.getPayTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Boolean isShopFirstBuy = service.checkShopFirstBuy(receive.getUserId(), receive.getShopId());
        Boolean isSpuFirstBuy = service.checkSpuFirstBuy(receive.getUserId(), receive.getSpuId());


        UserShopPayDetail shopPayDetail = new UserShopPayDetail();
        shopPayDetail.setUserId(userId);
        shopPayDetail.setShopId(receive.getShopId());
        shopPayDetail.setOrderId(orderId);
        shopPayDetail.setPayTime(receive.getPayTime());
        shopPayDetail.setIsFirstBuy(isShopFirstBuy ? (byte) USER_FIRST_BUY : (byte) USER_RETURN_BUY);
        shopPayDetail.setPayAmount(receive.getPrice());
        shopPayDetail.setIsValid((byte) ORDER_EFFECTIVE);
        shopPayDetail.setDt(dt);
        userShopPayDetailMapper.insert(shopPayDetail);

        UserSpuPayDetail spuPayDetail = new UserSpuPayDetail();
        spuPayDetail.setUserId(receive.getUserId());
        spuPayDetail.setSpuId(receive.getSpuId());
        spuPayDetail.setShopId(receive.getShopId());
        spuPayDetail.setOrderId(receive.getOrderId());
        spuPayDetail.setPayTime(receive.getPayTime());
        spuPayDetail.setIsFirstBuy(isSpuFirstBuy ? (byte) USER_FIRST_BUY : (byte) USER_RETURN_BUY);
        spuPayDetail.setPayAmount(receive.getPrice());
        spuPayDetail.setIsValid((byte) ORDER_EFFECTIVE);
        spuPayDetail.setDt(dt);
        userSpuPayDetailMapper.insert(spuPayDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSkuInShoppingList(Long userId, List<Long> cartIds) {
        // 1. 参数校验
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }
        
        // 2. 删除购物车中的指定商品（同时验证userId防止越权删除）
        new LambdaUpdateChainWrapper<>(userCartMapper)
                .eq(UserCart::getUserId, userId)
                .in(UserCart::getCartId, cartIds)
                .remove();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer checkIsFollowShop(Long userId, Long shopId) {
        LambdaQueryWrapper<UserShopFollow> userShopFollowLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userShopFollowLambdaQueryWrapper
                .eq(UserShopFollow::getUserId,userId)
                .eq(UserShopFollow::getShopId,shopId)
                .eq(UserShopFollow::getFollowStatus,USER_FOLLOW)
                .select(UserShopFollow::getId);

        UserShopFollow userShopFollow = userShopFollowMapper.selectOne(userShopFollowLambdaQueryWrapper);
        if(userShopFollow==null){
            return USER_NOT_FOLLOW;
        }else{
            return USER_FOLLOW;
        }

    }

    @Transactional(readOnly = true)
    @Override
    public Integer checkIsFollowSpu(Long userId, Long spuId) {
        LambdaQueryWrapper<UserGoodsFollow> userGoodsFollowLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userGoodsFollowLambdaQueryWrapper
                .eq(UserGoodsFollow::getUserId, userId)
                .eq(UserGoodsFollow::getSpuId, spuId)
                .eq(UserGoodsFollow::getFollowStatus, USER_FOLLOW)
                .select(UserGoodsFollow::getId);
        UserGoodsFollow userGoodsFollow = userGoodsFollowMapper.selectOne(userGoodsFollowLambdaQueryWrapper);
        if (userGoodsFollow == null) {
            return USER_NOT_FOLLOW;
        } else {
            return USER_FOLLOW;
        }
    }




    @Override
    @Transactional(readOnly = true)
    public SendCommentDTO getInformationForSendComment(Long userId, Long shopId,Long orderId, Long spuId, Long skuId) {
        LambdaQueryWrapper<UserDisplay> userDisplayLambdaQueryWrapper
                =new LambdaQueryWrapper<>();
        userDisplayLambdaQueryWrapper
                .eq(UserDisplay::getUserId,userId)
                .select(UserDisplay::getNickname,UserDisplay::getAvatarUrl);
        UserDisplay userDisplay = userDisplayMapper.selectOne(userDisplayLambdaQueryWrapper);

        // 2. 判断是否复购（检查该用户是否在该店铺有过购买记录）
        Boolean isShopFirstBuy = service.checkShopFirstBuy(userId, shopId );
        Integer isShopReturnCustomer = isShopFirstBuy ? USER_FIRST_BUY : USER_RETURN_BUY;

        // 3. 判断是否商品复购（检查该用户是否购买过该SPU）
        Boolean isSpuFirstBuy = service.checkSpuFirstBuy(userId, spuId);
        Integer isRepurchase = isSpuFirstBuy ? USER_FIRST_BUY : USER_RETURN_BUY;

        // 4. 构建返回对象
        SendCommentDTO dto = new SendCommentDTO();
        dto.setUserName(userDisplay.getNickname());
        dto.setUserAvatar(userDisplay.getAvatarUrl());
        dto.setIsRepurchase(isRepurchase);
        dto.setIsShopReturnCustomer(isShopReturnCustomer);

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDisplayInfo(UserDisplayAO ao, Long userId) {
        // 1. 构建更新对象
        UserDisplay userDisplay = new UserDisplay();
        userDisplay.setUserId(userId);
        if(StringUtils.isNotBlank(ao.getNickname())) {
            userDisplay.setNickname(ao.getNickname());
        }
        userDisplay.setGender(ao.getGender());
        userDisplay.setAvatarUrl(ao.getAvatarUrl());

        // 2. 使用 LambdaUpdateChainWrapper 进行更新（只更新非 null 字段）
        new LambdaUpdateChainWrapper<>(userDisplayMapper)
                .eq(UserDisplay::getUserId, userId)
                .update(userDisplay);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrivateInfo(UserPrivateAO ao, Long userId) {
        // 1. 构建更新对象
        UserPrivate userPrivate = new UserPrivate();
        userPrivate.setUserId(userId);
        userPrivate.setBirthday(ao.getBirthday());
        if(StringUtils.isNotBlank(ao.getPhone())) {
            userPrivate.setPhone(ao.getPhone());
        }
        if(StringUtils.isNotBlank(ao.getEmail())) {
            userPrivate.setEmail(ao.getEmail());
        }

        // 2. 使用 LambdaUpdateChainWrapper 进行更新（只更新非 null 字段）
        new LambdaUpdateChainWrapper<>(userPrivateMapper)
                .eq(UserPrivate::getUserId, userId)
                .update(userPrivate);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDisplayDTO getDisplayInfo(Long userId) {
        // 1. 查询展示信息
        UserDisplay userDisplay = userDisplayMapper.selectOne(
                new LambdaQueryWrapper<>(UserDisplay.class)
                        .eq(UserDisplay::getUserId, userId)
                        .select(UserDisplay::getUserId,
                                UserDisplay::getNickname,
                                UserDisplay::getGender,
                                UserDisplay::getAvatarUrl,
                                UserDisplay::getAccountStatus,
                                UserDisplay::getIsRealName,
                                UserDisplay::getRegisterTime)
        );

        if (userDisplay == null) {
            return null;
        }

        // 2. 转换为 DTO
        UserDisplayDTO dto = new UserDisplayDTO();
        dto.setUserId(userDisplay.getUserId());
        dto.setNickname(userDisplay.getNickname());
        dto.setGender(userDisplay.getGender());
        dto.setAvatarUrl(userDisplay.getAvatarUrl());
        dto.setAccountStatus(userDisplay.getAccountStatus());
        dto.setIsRealName(userDisplay.getIsRealName());
        dto.setRegisterTime(userDisplay.getRegisterTime());

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public UserPrivateDTO getPrivateInfo(Long userId) {
        // 1. 查询隐私信息
        UserPrivate userPrivate = userPrivateMapper.selectOne(
                new LambdaQueryWrapper<>(UserPrivate.class)
                        .eq(UserPrivate::getUserId, userId)
                        .select(UserPrivate::getUserId,
                                UserPrivate::getBirthday,
                                UserPrivate::getPhone,
                                UserPrivate::getEmail,
                                UserPrivate::getLastLoginTime,
                                UserPrivate::getCreateTime)
        );

        if (userPrivate == null) {
            return null;
        }

        // 2. 转换为 DTO（手机号和邮箱脱敏处理）
        UserPrivateDTO dto = new UserPrivateDTO();
        dto.setUserId(userPrivate.getUserId());
        dto.setBirthday(userPrivate.getBirthday());
        dto.setPhone(maskPhone(userPrivate.getPhone()));
        dto.setEmail(maskEmail(userPrivate.getEmail()));
        dto.setLastLoginTime(userPrivate.getLastLoginTime());
        dto.setCreateTime(userPrivate.getCreateTime());

        return dto;
    }

    /**
     * 手机号脱敏：保留前3位和后4位，中间用****代替
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 邮箱脱敏：保留@符号前的前2位和@符号后的部分，中间用***代替
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return email;
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

}
