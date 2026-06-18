package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class UserFollowSkuAO implements ValidationConstants {
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long skuId;


    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_BUY_NUM, message = PARAM_ERROR)
    private Integer buyNum;


}
