package com.daxi.converter;

import com.daxi.util.PriceUtil;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public class IntegerToBigDecimal {
    /**
     * Integer(分) → BigDecimal(元)
     */
    public BigDecimal integerFenToBigDecimalYuan(Integer priceFen) {
        return PriceUtil.fenToYuan(priceFen);
    }

    public BigDecimal LongFenToBigDecimalYuan(Long priceFen){
        return PriceUtil.fenToYuan(priceFen);
    }
    /**
     * BigDecimal(元) → Integer(分)
     */
    public Integer bigDecimalYuanToIntegerFen(BigDecimal priceYuan) {
        return PriceUtil.yuanToFen(priceYuan);
    }


}
