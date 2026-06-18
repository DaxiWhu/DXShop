package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class UserOrderAddressAO implements ValidationConstants {
    /**
     * 订单id
     * */
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long orderId;

    /**
     * 现收货人姓名
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_NAME_LENGTH, message = PARAM_ERROR)
    private String currentReceiverName;

    /**
     * 现收货人手机号
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_PHONE_LENGTH, message = PARAM_ERROR)
    private String currentReceiverPhone;

    /**
     * 现省份
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_REGION_LENGTH, message = PARAM_ERROR)
    private String currentProvince;

    /**
     * 现城市
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_REGION_LENGTH, message = PARAM_ERROR)
    private String currentCity;

    /**
     * 现区/县
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_REGION_LENGTH, message = PARAM_ERROR)
    private String currentDistrict;

    /**
     * 现详细地址
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_DETAIL_ADDRESS_LENGTH, message = PARAM_ERROR)
    private String currentDetailAddress;
}
