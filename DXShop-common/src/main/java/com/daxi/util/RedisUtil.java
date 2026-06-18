package com.daxi.util;

import com.daxi.domain.dto.AddressModifyRequestDTO;
import com.daxi.domain.dto.RefundRequestSimpleDTO;
import com.daxi.domain.dto.UserSimpleOrderDTO;
import com.daxi.domain.entity.GoodsSku;
import com.daxi.domain.entity.GoodsSpu;
import com.daxi.domain.entity.UserOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.daxi.key.redis.OrderKey.ORDER_CHANGE_ADDRESS_DETAIL;
import static com.daxi.key.redis.OrderKey.ORDER_CHANGE_ADDRESS_SHOP_REQUEST;
import static com.daxi.key.redis.OrderKey.ORDER_DETAIL;
import static com.daxi.key.redis.OrderKey.ORDER_SHOP_STATUS;
import static com.daxi.key.redis.OrderKey.ORDER_USER_ALL;
import static com.daxi.key.redis.OrderKey.ORDER_USER_STATUS;
import static com.daxi.key.redis.OrderKey.REFUND_DETAIL;
import static com.daxi.key.redis.OrderKey.REFUND_SHOP_LIST;
import static com.daxi.key.redis.OrderKey.REFUND_USER_LIST;
import static com.daxi.limit.OrderLimit.ORDERID_EXPIRE_SECONDS;
import static com.daxi.limit.OrderLimit.ORDER_CHANGE_ADDRESS_REQUEST_SECONDS;
import static com.daxi.limit.OrderLimit.ORDER_DETAIL_EXPIRE_MINUTES;
import static com.daxi.limit.OrderLimit.ORDER_STATUS_PENDING_PAYMENT;
import static com.daxi.limit.OrderLimit.REFUND_CACHE_EXPIRE_SECONDS;
import static com.daxi.limit.UserLimit.VERIFY_CODE_EXPIRE_SECONDS;
import static com.daxi.limit.UserLimit.VERIFY_CODE_INTERVAL_SECONDS;
import static com.daxi.key.redis.UserKey.VERIFY_CODE;
import static com.daxi.key.redis.UserKey.VERIFY_CODE_LIMIT;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    @Resource
    private final @NonNull StringRedisTemplate stringRedisTemplate;

    private final @NonNull ObjectMapper objectMapper;
    private static final RedisScript<Long> STOCK_SCRIPT;

    static {
        // 静态代码块里加载脚本，只执行一次
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/decrement.lua"));
        script.setResultType(Long.class);
        STOCK_SCRIPT = script;
    }


    // ========== 3. 原子扣库存 ==========
    public boolean deductStock(String stockKey, Integer buyNum) {
        // 执行 Lua
        Long result = stringRedisTemplate.execute(
                STOCK_SCRIPT,
                Collections.singletonList(stockKey),// KEYS[1]
                String.valueOf(buyNum)
        );

        // 1=成功，0=失败
        return result == 0;
    }

    // ========== 4. 缓存订单信息 ==========
    public void setOrderCache(UserSimpleOrderDTO order) {
        String cacheKey = ORDER_DETAIL.format(order.getOrderId());
        Map<String, String> map = new HashMap<>();
        map.put("goodsName", order.getGoodsName());
        map.put("goodsImg", order.getGoodsImg());
        map.put("skuSpec", order.getSkuSpec());
        map.put("perPrice", order.getPerPrice().toString());
        map.put("buyNum", order.getBuyNum().toString());
        map.put("payAmount", order.getPayAmount().toString());
        map.put("status", String.valueOf(order.getStatus()));
        map.put("operateStatus",String.valueOf(order.getOperateStatus()));

        stringRedisTemplate.opsForHash().putAll(cacheKey, map);
        stringRedisTemplate.expire(cacheKey, ORDER_DETAIL_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
    public UserSimpleOrderDTO getOrderCache(Long orderId) {
        String cacheKey = ORDER_DETAIL.format(orderId);
        Map<Object, Object> result = stringRedisTemplate.opsForHash().entries(cacheKey);
        return objectMapper.convertValue(result, UserSimpleOrderDTO.class);
    }
    public void deleteOrderCache(String orderId,String userId) {
        String cacheKey = ORDER_DETAIL.format(orderId);
        String userAllKey = ORDER_USER_ALL.format(userId);
        String userStatusKey = ORDER_USER_STATUS.format(userId, ORDER_STATUS_PENDING_PAYMENT);
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.execute("DEL", cacheKey.getBytes(StandardCharsets.UTF_8));
            connection.execute("DEL", userAllKey.getBytes(StandardCharsets.UTF_8));
            connection.execute("DEL", userStatusKey.getBytes(StandardCharsets.UTF_8));
            return null;
        });
    }
    public void deleteDetailOrderCache(Long orderId){
        String cacheKey = ORDER_DETAIL.format(orderId);
        stringRedisTemplate.delete(cacheKey);
    }
    //迁移订单索引缓存
    public void changeOrderIndex(String orderId, String userId, String shopId, int originStatus, int newStatus) {
        String userStatusKey = ORDER_USER_STATUS.format(userId, originStatus);
        String shopStatusKey = ORDER_SHOP_STATUS.format(shopId, originStatus);
        String userStatusKeyNew = ORDER_USER_STATUS.format(userId, newStatus);
        String shopStatusKeyNew = ORDER_SHOP_STATUS.format(shopId, newStatus);
        long score = System.currentTimeMillis();
        byte[] id = orderId.getBytes();

        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.zSetCommands().zRem(userStatusKey.getBytes(StandardCharsets.UTF_8), id);
            connection.zSetCommands().zRem(shopStatusKey.getBytes(StandardCharsets.UTF_8), id);
            connection.zSetCommands().zAdd(userStatusKeyNew.getBytes(StandardCharsets.UTF_8), score, id);
            connection.zSetCommands().zAdd(shopStatusKeyNew.getBytes(StandardCharsets.UTF_8), score, id);

            byte[] ttlBytes = String.valueOf(ORDERID_EXPIRE_SECONDS).getBytes(StandardCharsets.UTF_8);
            connection.execute("EXPIRE",
                    userStatusKeyNew.getBytes(StandardCharsets.UTF_8),
                    ttlBytes);

            connection.execute("EXPIRE",
                    shopStatusKeyNew.getBytes(StandardCharsets.UTF_8),
                    ttlBytes);
            return null;
        });
    }

    public void cacheOrderIndex(UserOrder order, GoodsSpu spu, GoodsSku sku, Integer buyNum) {
        String userAllKey = ORDER_USER_ALL.format(order.getUserId());
        String userStatusKey = ORDER_USER_STATUS.format(order.getUserId(), order.getOrderStatus());
        String shopStatusKey = ORDER_SHOP_STATUS.format(order.getShopId(), order.getOrderStatus());

        long score = System.currentTimeMillis();
        String value = order.getOrderId().toString();

        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.zSetCommands().zAdd(userAllKey.getBytes(StandardCharsets.UTF_8), score, value.getBytes(StandardCharsets.UTF_8));
            connection.zSetCommands().zAdd(userStatusKey.getBytes(StandardCharsets.UTF_8), score, value.getBytes(StandardCharsets.UTF_8));
            connection.zSetCommands().zAdd(shopStatusKey.getBytes(StandardCharsets.UTF_8), score, value.getBytes(StandardCharsets.UTF_8));
            byte[] ttlBytes = String.valueOf(ORDERID_EXPIRE_SECONDS).getBytes(StandardCharsets.UTF_8);

            connection.execute("EXPIRE",
                    userAllKey.getBytes(StandardCharsets.UTF_8),
                    ttlBytes);

            connection.execute("EXPIRE",
                    userStatusKey.getBytes(StandardCharsets.UTF_8),
                    ttlBytes);

            connection.execute("EXPIRE",
                    shopStatusKey.getBytes(StandardCharsets.UTF_8),
                    ttlBytes);
            return null;
        });

        // 完整映射 DTO，不依赖主表未存在的字段
        UserSimpleOrderDTO dto = new UserSimpleOrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setGoodsName(spu.getTitle());      // 从 SPU 对象取
        dto.setGoodsImg(spu.getMainImg());      // 从 SPU 对象取
        dto.setSkuSpec(sku.getSkuSpec());       // 从 SKU 对象取
        dto.setPerPrice(PriceUtil.fenToYuan(sku.getPrice()));              // 使用计算出的单价
        dto.setBuyNum(buyNum);                       // 根据你的 AO 逻辑，如果是单SKU下单
        dto.setPayAmount(order.getPrice());     // 总价
        dto.setStatus(String.valueOf(order.getOrderStatus()));
        dto.setOperateStatus(order.getOperateStatus());
        setOrderCache(dto);
    }

    public void cacheRefund(String orderId, String userId, String shopId, int refundStatus) {
        String shopRefundKey = REFUND_SHOP_LIST.format(shopId, refundStatus);
        String userRefundKey = REFUND_USER_LIST.format(userId, refundStatus);

        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.zSetCommands().zAdd(shopRefundKey.getBytes(StandardCharsets.UTF_8),
                    System.currentTimeMillis(),
                    (orderId).getBytes(StandardCharsets.UTF_8));
            connection.execute("EXPIRE",
                    shopRefundKey.getBytes(StandardCharsets.UTF_8),
                    String.valueOf(REFUND_CACHE_EXPIRE_SECONDS).getBytes(StandardCharsets.UTF_8));

            connection.zSetCommands().zAdd(userRefundKey.getBytes(StandardCharsets.UTF_8),
                    System.currentTimeMillis(),
                    (orderId).getBytes(StandardCharsets.UTF_8));
            connection.execute("EXPIRE",
                    userRefundKey.getBytes(StandardCharsets.UTF_8),
                    String.valueOf(REFUND_CACHE_EXPIRE_SECONDS).getBytes(StandardCharsets.UTF_8));
            return null;
        });
    }

    public void changeAddressRequestCache(String orderId, String userId, String shopId, int originStatus, int newStatus) {
        String userStatusKey = ORDER_USER_STATUS.format(userId, originStatus);
        String shopStatusKey = ORDER_SHOP_STATUS.format(shopId, originStatus);
        String userStatusKeyNew = ORDER_USER_STATUS.format(userId, newStatus);
        String shopStatusKeyNew = ORDER_SHOP_STATUS.format(shopId, newStatus);
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.zSetCommands().zRem(userStatusKey.getBytes(StandardCharsets.UTF_8), orderId.getBytes(StandardCharsets.UTF_8));
            connection.zSetCommands().zRem(shopStatusKey.getBytes(StandardCharsets.UTF_8), orderId.getBytes(StandardCharsets.UTF_8));
            connection.zSetCommands().zAdd(userStatusKeyNew.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis(), orderId.getBytes(StandardCharsets.UTF_8));
            connection.zSetCommands().zAdd(shopStatusKeyNew.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis(), orderId.getBytes(StandardCharsets.UTF_8));
            connection.execute("EXPIRE",
                    userStatusKeyNew.getBytes(StandardCharsets.UTF_8),
                    String.valueOf(ORDER_CHANGE_ADDRESS_REQUEST_SECONDS).getBytes(StandardCharsets.UTF_8));
            connection.execute("EXPIRE",
                    ORDER_CHANGE_ADDRESS_SHOP_REQUEST.format(shopId, newStatus).getBytes(StandardCharsets.UTF_8),
                    shopStatusKeyNew.getBytes(StandardCharsets.UTF_8));
            return null;
        });
    }

    public void changeRefundCache(String orderId, String userId, String shopId, int originStatus, int newStatus) {
        String userRefundKey = REFUND_USER_LIST.format(userId, originStatus);
        String shopRefundKey = REFUND_SHOP_LIST.format(shopId, originStatus);
        String userRefundKeyNew = REFUND_USER_LIST.format(userId, newStatus);
        String shopRefundKeyNew = REFUND_SHOP_LIST.format(shopId, newStatus);
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.zSetCommands().zRem(userRefundKey.getBytes(StandardCharsets.UTF_8), orderId.getBytes(StandardCharsets.UTF_8));
            connection.zSetCommands().zRem(shopRefundKey.getBytes(StandardCharsets.UTF_8), orderId.getBytes(StandardCharsets.UTF_8));
            connection.zSetCommands().zAdd(userRefundKeyNew.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis(), orderId.getBytes(StandardCharsets.UTF_8));
            connection.zSetCommands().zAdd(shopRefundKeyNew.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis(), orderId.getBytes(StandardCharsets.UTF_8));
            connection.execute("EXPIRE",
                    userRefundKeyNew.getBytes(StandardCharsets.UTF_8),
                    String.valueOf(REFUND_CACHE_EXPIRE_SECONDS).getBytes(StandardCharsets.UTF_8));
            connection.execute("EXPIRE",
                    shopRefundKeyNew.getBytes(StandardCharsets.UTF_8),
                    String.valueOf(REFUND_CACHE_EXPIRE_SECONDS).getBytes(StandardCharsets.UTF_8));
            return null;
        });
    }

    // ========== 缓存地址修改申请 ==========
    public void setAddressModifyCache(AddressModifyRequestDTO dto) {
        String cacheKey = ORDER_CHANGE_ADDRESS_DETAIL.format(dto.getOrderId());
        Map<String, String> map = new HashMap<>();
        map.put("orderId", String.valueOf(dto.getOrderId()));
        map.put("userId", String.valueOf(dto.getUserId()));
        map.put("shopId", String.valueOf(dto.getShopId()));
        map.put("oldName", dto.getOldName());
        map.put("oldPhone", dto.getOldPhone());
        map.put("oldDetail", dto.getOldDetail());
        map.put("newName", dto.getNewName());
        map.put("newPhone", dto.getNewPhone());
        map.put("newDetail", dto.getNewDetail());
        map.put("status", String.valueOf(dto.getStatus()));
        map.put("expireTime", String.valueOf(dto.getExpireTime()));
        map.put("createTime", String.valueOf(dto.getCreateTime()));
        
        stringRedisTemplate.opsForHash().putAll(cacheKey, map);
        stringRedisTemplate.expire(cacheKey, ORDER_DETAIL_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
    public void deleteAddressModifyCache(String orderId) {
        stringRedisTemplate.delete(ORDER_CHANGE_ADDRESS_DETAIL.format(orderId));
    }
    public AddressModifyRequestDTO getAddressModifyCache(Map<Object, Object> result) {
        Map<Object, Object> hashData = (Map<Object, Object>) result;
        AddressModifyRequestDTO dto = new AddressModifyRequestDTO();
        dto.setOrderId(Long.valueOf(String.valueOf(hashData.get("orderId"))));
        dto.setUserId(Long.valueOf(String.valueOf(hashData.get("userId"))));
        dto.setShopId(Long.valueOf(String.valueOf(hashData.get("shopId"))));
        dto.setOldName(String.valueOf(hashData.get("oldName")));
        dto.setOldPhone(String.valueOf(hashData.get("oldPhone")));
        dto.setOldDetail(String.valueOf(hashData.get("oldDetail")));
        dto.setNewName(String.valueOf(hashData.get("newName")));
        dto.setNewPhone(String.valueOf(hashData.get("newPhone")));
        dto.setNewDetail(String.valueOf(hashData.get("newDetail")));
        dto.setStatus(Integer.valueOf(String.valueOf(hashData.get("status"))));
        return dto;
        }
    // ========== 缓存退款申请 ==========
    public void setRefundCache(RefundRequestSimpleDTO dto) {
        String cacheKey = REFUND_DETAIL.format(dto.getId());
        Map<String, String> map = new HashMap<>();
        map.put("refundNo", dto.getRefundNo());
        map.put("refundType", String.valueOf(dto.getRefundType()));
        map.put("refundAmount", String.valueOf(dto.getRefundAmount()));
        map.put("refundReason", dto.getRefundReason());
        map.put("status", String.valueOf(dto.getStatus()));
        map.put("applyTime", String.valueOf(dto.getApplyTime()));
        map.put("auditTime", String.valueOf(dto.getAuditTime()));
        map.put("refundTime", String.valueOf(dto.getRefundTime()));
        map.put("failReason", dto.getFailReason());

        stringRedisTemplate.opsForHash().putAll(cacheKey, map);
        stringRedisTemplate.expire(cacheKey, ORDER_DETAIL_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }
    public void deleteRefundCache(Long id ) {
        stringRedisTemplate.delete(REFUND_DETAIL.format(id));
    }
    // ========== 验证码相关 ==========

    /**
     * 存储验证码到 Redis，并设置发送间隔限制
     * @param phone 手机号
     * @param code  验证码
     */
    public void setVerifyCode(String phone, String code) {
        String codeKey = VERIFY_CODE.format(phone);
        String limitKey = VERIFY_CODE_LIMIT.format(phone);
        stringRedisTemplate.opsForValue().set(codeKey, code, VERIFY_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(limitKey, "1", VERIFY_CODE_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 获取验证码
     * @param phone 手机号
     * @return 验证码，不存在或已过期返回 null
     */
    public String getVerifyCode(String phone) {
        String codeKey = VERIFY_CODE.format(phone);
        return stringRedisTemplate.opsForValue().get(codeKey);
    }

    /**
     * 删除验证码（验证成功后调用）
     * @param phone 手机号
     */
    public void deleteVerifyCode(String phone) {
        String codeKey = VERIFY_CODE.format(phone);
        stringRedisTemplate.delete(codeKey);
    }

    /**
     * 判断是否在发送间隔内（防刷）
     * @param phone 手机号
     * @return true=仍在间隔期内（不能发送），false=可以发送
     */
    public boolean isVerifyCodeLimited(String phone) {
        String limitKey = VERIFY_CODE_LIMIT.format(phone);
        return stringRedisTemplate.hasKey(limitKey);
    }

    public RefundRequestSimpleDTO getRefundCache(Map<Object, Object> result) {

        Map<Object, Object> hashData = (Map<Object, Object>) result;
        RefundRequestSimpleDTO dto = new RefundRequestSimpleDTO();
        dto.setRefundNo(String.valueOf(hashData.get("refundNo")));
        dto.setRefundType(Integer.valueOf(String.valueOf(hashData.get("refundType"))));
        dto.setRefundAmount(new BigDecimal(String.valueOf(hashData.get("refundAmount"))));
        dto.setRefundReason(String.valueOf(hashData.get("refundReason")));
        dto.setStatus(Integer.valueOf(String.valueOf(hashData.get("status"))));
        dto.setApplyTime(LocalDateTime.parse(String.valueOf(hashData.get("applyTime"))));
        dto.setAuditTime(LocalDateTime.parse(String.valueOf(hashData.get("auditTime"))));
        dto.setRefundTime(LocalDateTime.parse(String.valueOf(hashData.get("refundTime"))));
        dto.setFailReason(String.valueOf(hashData.get("failReason")));
        return dto;
    }
}