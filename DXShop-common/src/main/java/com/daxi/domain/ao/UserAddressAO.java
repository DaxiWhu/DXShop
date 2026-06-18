package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;
import org.hibernate.validator.constraints.Length;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class UserAddressAO implements ValidationConstants {
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long addressId;
    

    /**
     * 收货人姓名
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_NAME_LENGTH, message = PARAM_ERROR)
    private String receiverName;

    /**
     * 收货人手机号
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_PHONE_LENGTH, message = PARAM_ERROR)
    private String receiverPhone;

    /**
     * 省份
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_REGION_LENGTH, message = PARAM_ERROR)
    private String province;

    /**
     * 城市
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_REGION_LENGTH, message = PARAM_ERROR)
    private String city;

    /**
     * 区/县
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_REGION_LENGTH, message = PARAM_ERROR)
    private String district;

    /**
     * 详细地址
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_DETAIL_ADDRESS_LENGTH, message = PARAM_ERROR)
    private String detailAddress;

    /**
     * 1=默认地址 0=普通地址
     */
    @NonNull
    @Min(value = IS_NOT_DEFAULT, message = PARAM_ERROR)
    @Max(value = IS_DEFAULT, message = PARAM_ERROR)
    private Integer isDefault;

    /**
     * 地址标签：家/公司/学校
     */
    @Length(max = MAX_TAG_LENGTH, message = PARAM_ERROR)
    private String tag;

}
