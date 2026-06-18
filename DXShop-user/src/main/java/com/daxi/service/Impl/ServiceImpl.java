package com.daxi.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.daxi.Exception.BusinessException;
import com.daxi.domain.entity.UserShopPayDetail;
import com.daxi.domain.entity.UserSpuPayDetail;
import com.daxi.mapper.UserShopPayDetailMapper;
import com.daxi.mapper.UserSpuPayDetailMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Service
@RequiredArgsConstructor
public class ServiceImpl {
    private @NonNull final UserSpuPayDetailMapper userSpuPayDetailMapper;
    private @NonNull final UserShopPayDetailMapper userShopPayDetailMapper;
    public Boolean checkShopFirstBuy(Long userId, Long shopId) {
        boolean exists = userShopPayDetailMapper.exists(
                new LambdaQueryWrapper<UserShopPayDetail>()
                        .eq(UserShopPayDetail::getUserId, userId)
                        .eq(UserShopPayDetail::getShopId, shopId)
        );
        return !exists;
    }

    public Boolean checkSpuFirstBuy(Long userId, Long spuId) {
        boolean exists = userSpuPayDetailMapper.exists(
                new LambdaQueryWrapper<UserSpuPayDetail>()
                        .eq(UserSpuPayDetail::getUserId, userId)
                        .eq(UserSpuPayDetail::getSpuId, spuId)
        );
        return !exists;
    }
    public void validateBusinessHours(String businessHours) {
        if (businessHours == null || businessHours.isEmpty()) {
            return; // 允许为空
        }

        // 正则表达式验证基本格式
        String regex = "^([0-2]\\d:[0-5]\\d-[0-2]\\d:[0-5]\\d)(,[0-2]\\d:[0-5]\\d-[0-2]\\d:[0-5]\\d)*$";
        if (!Pattern.matches(regex, businessHours)) {
            throw new BusinessException(PARAM_ERROR);
        }

        // 分割多个时间段进行详细验证
        String[] periods = businessHours.split(",");

        // 验证时间段数量（最多支持5个时段）
        if (periods.length > 5) {
            throw new BusinessException(PARAM_ERROR);
        }

        for (int i = 0; i < periods.length; i++) {
            String period = periods[i];
            String[] times = period.split("-");

            if (times.length != 2) {
                throw new BusinessException(PARAM_ERROR);
            }

            String startTime = times[0];
            String endTime = times[1];

            // 验证时间格式
            if (isValidTimeFormat(startTime) || isValidTimeFormat(endTime)) {
                throw new BusinessException(PARAM_ERROR);
            }

            // 验证结束时间不能早于或等于开始时间
            if (!isTimeAfter(endTime, startTime)) {
                throw new BusinessException(PARAM_ERROR);
            }
        }

        // 验证时段之间是否有重叠
        if (!validateNoOverlap(periods)) {
            throw new BusinessException(PARAM_ERROR);
        }
    }
    private boolean isValidTimeFormat(String time) {
        String[] parts = time.split(":");
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            return hour < 0 || hour > 23 || minute < 0 || minute > 59;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * 判断 time1 是否晚于 time2
     */
    private boolean isTimeAfter(String time1, String time2) {
        int minutes1 = convertToMinutes(time1);
        int minutes2 = convertToMinutes(time2);
        return minutes1 > minutes2;
    }
    private int convertToMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;

    }
    /**
     * 验证时段之间没有重叠
     */
    private boolean validateNoOverlap(String[] periods) {
        if (periods.length <= 1) {
            return true;
        }

        // 将所有时段转换为分钟数对并排序
        int[][] timeRanges = new int[periods.length][2];
        for (int i = 0; i < periods.length; i++) {
            String[] times = periods[i].split("-");
            timeRanges[i][0] = convertToMinutes(times[0]); // 开始时间
            timeRanges[i][1] = convertToMinutes(times[1]); // 结束时间
        }

        // 按开始时间排序
        Arrays.sort(timeRanges, Comparator.comparingInt(a -> a[0]));

        // 检查是否有重叠
        for (int i = 0; i < timeRanges.length - 1; i++) {
            // 如果当前时段的结束时间 > 下一个时段的开始时间，则有重叠
            if (timeRanges[i][1] > timeRanges[i + 1][0]) {
                return false;
            }
        }

        return true;
    }

    public boolean validShopType(@NotBlank Integer shopType) {
        if(shopType>=1 && shopType<=4) {
            return true;
        }else{
            return false;
        }
    }

    public boolean validShopStatus(Integer shopStatus) {
        if(shopStatus>=0 && shopStatus<=3) {
            return true;
        }else {
            return false;
        }
    }
}
