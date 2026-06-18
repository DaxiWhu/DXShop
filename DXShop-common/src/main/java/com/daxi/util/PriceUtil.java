package com.daxi.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceUtil {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    public static Integer yuanToFen(BigDecimal yuan){
        if (yuan == null) {
            return 0;
        }
        // 元 ×100 → 四舍五入 → 转为整数
        return yuan.multiply(ONE_HUNDRED)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact(); // 严格转int，超出范围抛异常（更安全）
    }
    public static BigDecimal fenToYuan(BigDecimal fen){
        if (fen == null) {
            return BigDecimal.ZERO;
        }
        return fen.divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
    public static BigDecimal fenToYuan(Integer  fen){
        if (fen == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(fen).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
    public static BigDecimal fenToYuan(Long fen) {
        if (fen == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(fen).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }
}
