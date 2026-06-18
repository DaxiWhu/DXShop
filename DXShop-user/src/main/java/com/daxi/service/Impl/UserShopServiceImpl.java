package com.daxi.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.daxi.Exception.BusinessException;
import com.daxi.converter.UserShopDisplayToSImpleDtoConverter;
import com.daxi.domain.ao.GetShopCreateRequestPageAO;
import com.daxi.domain.ao.UserShopAO;
import com.daxi.domain.ao.UserShopCreateAO;
import com.daxi.domain.dto.LoginResponseDTO;
import com.daxi.domain.dto.UserShopDTO;
import com.daxi.domain.dto.UserShopSimpleDTO;
import com.daxi.domain.entity.ShopChangeRequest;
import com.daxi.domain.entity.UserShopDisplay;
import com.daxi.domain.entity.UserShopExtend;
import com.daxi.mapper.ShopChangeRequestMapper;
import com.daxi.mapper.UserShopDisplayMapper;
import com.daxi.mapper.UserShopExtendMapper;
import com.daxi.service.UserShopService;
import com.daxi.util.JwtUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.daxi.key.redis.UserKey.SHOP_LOGIN;
import static com.daxi.limit.UserLimit.LOGIN_TOKEN_PREFIX;
import static com.daxi.limit.UserLimit.SHOP_CHANGE_REQUEST_STATUS_APPROVED;
import static com.daxi.limit.UserLimit.SHOP_CHANGE_REQUEST_STATUS_REJECTED;
import static com.daxi.limit.UserLimit.SHOP_CHANGE_REQUEST_STATUS_WAIT;
import static com.daxi.limit.UserLimit.SHOP_STATUS_AUDITING;
import static com.daxi.limit.UserLimit.SHOP_STATUS_CLOSED;
import static com.daxi.limit.UserLimit.SHOP_STATUS_NORMAL;
import static com.daxi.limit.UserLimit.USER_LOGIN_EFFECTIVE_DAY;
import static com.daxi.response.OrderResponse.PARAM_ERROR;
import static com.daxi.response.UserShopResponse.USER_SHOP_UNDATE_ON;
import static com.daxi.response.UserShopResponse.USER_SHOP_UNDATE_SUCCESS;


@Service
@RequiredArgsConstructor
public class UserShopServiceImpl implements UserShopService {
    private final @NonNull UserShopDisplayMapper userShopDisplayMapper;
    private final @NonNull UserShopExtendMapper userShopExtendMapper;
    private final @NonNull ShopChangeRequestMapper shopChangeRequestMapper;
    private final @NonNull ServiceImpl serviceImpl;
    private final @NonNull StringRedisTemplate stringRedisTemplate;
    private final @NonNull UserShopDisplayToSImpleDtoConverter userShopDisplayToSImpleDtoConverter;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateShop(Long shopId, UserShopAO ao) {

        String result=USER_SHOP_UNDATE_SUCCESS;
        ShopChangeRequest shopChangeRequest = new ShopChangeRequest();

        UserShopDisplay userShopDisplay = new UserShopDisplay();
        userShopDisplay.setShopId(shopId);
        if(StringUtils.isNotBlank(ao.getShopName())) {
            userShopDisplay.setShopName(ao.getShopName());
        }
        Integer shopType = ao.getShopType();
        if(shopType!=null&&serviceImpl.validShopType(shopType)){
                //写入申请
            shopChangeRequest.setId(IdWorker.getId());
            shopChangeRequest.setShopId(shopId);
            shopChangeRequest.setShopType(shopType);
            shopChangeRequest.setStatus(SHOP_CHANGE_REQUEST_STATUS_WAIT);
            result=USER_SHOP_UNDATE_ON;
        }

        if(StringUtils.isNotBlank(ao.getLogoUrl())){
            userShopDisplay.setLogoUrl(ao.getLogoUrl());
        }
        if(ao.getUserId() != null){
            if(shopChangeRequest.getId()==null){
                shopChangeRequest.setId(IdWorker.getId());
                shopChangeRequest.setShopId(shopId);
                shopChangeRequest.setStatus(SHOP_CHANGE_REQUEST_STATUS_WAIT);
            }
            shopChangeRequest.setUserId(ao.getUserId());
            result=USER_SHOP_UNDATE_ON;
        }
        new LambdaUpdateChainWrapper<>(userShopDisplayMapper)
                .update(userShopDisplay);

        UserShopExtend userShopExtend = new UserShopExtend();

        userShopExtend.setShopId(shopId);

        if(StringUtils.isNotBlank(ao.getShopDesc())) {
            userShopExtend.setShopDesc(ao.getShopDesc());
        }
        if(StringUtils.isNotBlank(ao.getBusinessHours())) {
            // 验证营业时间格式和逻辑
            serviceImpl.validateBusinessHours(ao.getBusinessHours());
            userShopExtend.setBusinessHours(ao.getBusinessHours());
        }
        if(StringUtils.isNotBlank(ao.getContactPhone())){
            userShopExtend.setContactPhone(ao.getContactPhone());
        }
        if(StringUtils.isNotBlank(ao.getContactEmail())){
            userShopExtend.setContactEmail(ao.getContactEmail());
        }
        if(StringUtils.isNotBlank(ao.getAddress())){
            userShopExtend.setAddress(ao.getAddress());
        }
        new LambdaUpdateChainWrapper<>(userShopExtendMapper)
                .update(userShopExtend);
        if(shopChangeRequest.getId()!=null) {
            shopChangeRequestMapper.insert(shopChangeRequest);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public UserShopDisplay getShopShowById(Long shopId) {
        LambdaQueryWrapper<UserShopDisplay> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserShopDisplay::getShopId, shopId);
        return userShopDisplayMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(readOnly = true)
    public UserShopDTO getShopAllById(Long shopId) {
       return userShopExtendMapper.getShopAllById(shopId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserShopSimpleDTO> getMyShopByuserId(Long userId) {
        LambdaQueryWrapper<UserShopDisplay> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserShopDisplay::getUserId, userId)
                .select(
                        UserShopDisplay::getShopId,
                        UserShopDisplay::getShopName,
                        UserShopDisplay::getLogoUrl,
                        UserShopDisplay::getShopStatus
                );
        return userShopDisplayToSImpleDtoConverter
                .toDtoList(userShopDisplayMapper.selectList(queryWrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createShop(UserShopCreateAO ao, Long userId) {
        if(!serviceImpl.validShopType(ao.getShopType())){
            throw new BusinessException(PARAM_ERROR);
        }
        // 生成店铺ID
        Long shopId = IdWorker.getId();

        // 创建店铺基本信息
        UserShopDisplay userShopDisplay = new UserShopDisplay();
        userShopDisplay.setShopId(shopId);
        userShopDisplay.setUserId(userId);
        userShopDisplay.setShopName(ao.getShopName());
        userShopDisplay.setShopType(ao.getShopType()); // 默认设置为个人店
        userShopDisplay.setShopStatus(SHOP_STATUS_AUDITING); // 默认设置为审核中
        userShopDisplay.setLogoUrl(ao.getLogoUrl());
        // 创建店铺扩展信息
        UserShopExtend userShopExtend = new UserShopExtend();
        userShopExtend.setShopId(shopId);
        userShopExtend.setShopDesc(ao.getShopDesc());
        userShopExtend.setBusinessHours(ao.getBusinessHours());
        userShopExtend.setContactPhone(ao.getContactPhone());
        userShopExtend.setContactEmail(ao.getContactEmail());
        userShopExtend.setAddress(ao.getAddress());

        // 插入数据
        userShopDisplayMapper.insert(userShopDisplay);
        userShopExtendMapper.insert(userShopExtend);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO loginShop(Long shopId, Long userId) {
        LambdaQueryWrapper<UserShopDisplay> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserShopDisplay::getShopId, shopId)
                .eq(UserShopDisplay::getUserId, userId)
                .select(UserShopDisplay::getShopId);
        if(userShopDisplayMapper.selectOne(queryWrapper)==null){
            return null;
        }
        String token = JwtUtil.generateToken(String.valueOf(shopId), null);
        stringRedisTemplate.opsForValue().set(SHOP_LOGIN.format(userId), token);
        stringRedisTemplate.expire(SHOP_LOGIN.format(userId), USER_LOGIN_EFFECTIVE_DAY, TimeUnit.DAYS);

        // 7. 构建返回结果
        return LoginResponseDTO.builder()
                .shopId(shopId)
                .token(LOGIN_TOKEN_PREFIX+token)
                .build();
    }

    @Override
    public void auditShop(Long shopId, Integer result) {
        int status;
        if(result==0){ //审核不通过
            status = SHOP_STATUS_CLOSED;
        }else if(result==1){
            status = SHOP_STATUS_NORMAL;
        }else{
            throw new BusinessException(PARAM_ERROR);
        }
        new LambdaUpdateChainWrapper<>(userShopDisplayMapper)
                .eq(UserShopDisplay::getShopId, shopId)
                .set(UserShopDisplay::getShopStatus, status)
                .update();

    }

    @Override
    @Transactional(readOnly = true)
    public List<UserShopDTO> getShopAudit(GetShopCreateRequestPageAO ao) {
        int index= (ao.getPageNum()-1)*ao.getPageSize();
        List<UserShopDTO> dtos= userShopExtendMapper.getShopAudit(index,ao.getPageSize());
        if(CollUtil.isEmpty(dtos)){
            return Collections.emptyList();
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopChangeRequest> getShopChangeRequest(Long shopId) {
        LambdaQueryWrapper<ShopChangeRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopChangeRequest::getShopId, shopId)
                .orderByDesc(ShopChangeRequest::getCreateTime);
        List<ShopChangeRequest> shopChangeRequests = shopChangeRequestMapper.selectList(queryWrapper);
        if(shopChangeRequests== null){
            return Collections.emptyList();
        }
        return shopChangeRequests;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopChangeRequest> getShopChangeRequestPageForAudit(Integer pageNum, Integer pageSize) {
        Page<ShopChangeRequest> shopChangeRequestPage = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ShopChangeRequest> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopChangeRequest::getStatus,SHOP_CHANGE_REQUEST_STATUS_WAIT )
                .orderByDesc(ShopChangeRequest::getCreateTime);
        Page<ShopChangeRequest> shopChangeRequestPage1 = shopChangeRequestMapper.selectPage(shopChangeRequestPage, queryWrapper);
        List<ShopChangeRequest> records = shopChangeRequestPage1.getRecords();
        if(CollUtil.isEmpty(records)){
            return Collections.emptyList();
        }
        return records;
    }

    @Override
    public void auditShopChange(Long id, Integer result) {
        if(result!=0&&result!=1){
            throw new BusinessException(PARAM_ERROR);
        }
        int status;
        if(result==0){
            status=SHOP_CHANGE_REQUEST_STATUS_REJECTED;
        }else{
            status=SHOP_CHANGE_REQUEST_STATUS_APPROVED;
        }
        new LambdaUpdateChainWrapper<>(shopChangeRequestMapper)
                .eq(ShopChangeRequest::getId, id)
                .eq(ShopChangeRequest::getStatus, SHOP_CHANGE_REQUEST_STATUS_WAIT)
                .set(ShopChangeRequest::getStatus, status)
                .update();
    }

    /**
     * 验证营业时间格式和逻辑
     * 支持格式：
     * - 单时段：09:00-22:00
     * - 多时段：09:00-12:00,14:00-18:00
     * - 空字符串表示不营业
     */

    /**
     * 验证时间格式是否为 HH:MM
     */

    
    /**
     * 将时间字符串转换为分钟数（从00:00开始）
     */

    


}
