package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class UserSimpleOrderAO implements ValidationConstants {

    /**
     * 订单状态：-1=全部, 0=待支付, 1=待发货, 2=待收货, 3=待评价, 4=已完成, 5=已取消, 6=售后
     */
    @NotNull(message = PARAM_ERROR)
    @Min(value = ORDER_STATUS_ALL, message = PARAM_ERROR)
    @Max(value = ORDER_STATUS_REFUND_AFTER_SALES, message = PARAM_ERROR)
    private Integer status;

    /**
     * 每页数量
     */
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_PAGE_SIZE, message = PARAM_ERROR)
    @Max(value = MAX_PAGE_SIZE, message = PARAM_ERROR)
    private Integer pageSize;

    /**
     * 页码
     */
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_PAGE_NUM, message = PARAM_ERROR)
    private Integer pageNum;
}
