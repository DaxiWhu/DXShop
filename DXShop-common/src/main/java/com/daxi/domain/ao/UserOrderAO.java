package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class UserOrderAO implements ValidationConstants {
    // 商品SKU ID
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long skuId;

    // 购买数量
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_BUY_NUM, message = PARAM_ERROR)
    private Integer buyNum;

    // 收货人姓名
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_NAME_LENGTH, message = PARAM_ERROR)
    private String receiverName;

    // 收货人手机
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_PHONE_LENGTH, message = PARAM_ERROR)
    private String receiverPhone;

    // 收货地址
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_ADDRESS_LENGTH, message = PARAM_ERROR)
    private String receiverAddress;

    // 备注
    @Length(max = MAX_REMARK_LENGTH, message = PARAM_ERROR)
    private String remark;
}
