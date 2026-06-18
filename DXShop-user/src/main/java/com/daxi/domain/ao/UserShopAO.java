package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class UserShopAO implements ValidationConstants {

    @Length(max = MAX_SHOP_NAME_LENGTH, message = PARAM_ERROR)
    private String shopName;

    private Integer shopType;



    @Length(max = MAX_URL_LENGTH, message = PARAM_ERROR)
    private String logoUrl;

    @Min(value =MIN_ID, message = PARAM_ERROR )
    private Long userId;

    @Length(max = MAX_SHOP_DESC_LENGTH, message = PARAM_ERROR)
    private String shopDesc;

    @Pattern(regexp = "^$|^([0-2]\\d:[0-5]\\d-[0-2]\\d:[0-5]\\d)(,[0-2]\\d:[0-5]\\d-[0-2]\\d:[0-5]\\d)*$", message = PARAM_ERROR)
    @Length(max = MAX_BUSINESS_HOURS_LENGTH, message = PARAM_ERROR)
    private String businessHours;

    @Pattern(regexp = "^$|^[1][3-9]\\d{9}$", message = PARAM_ERROR)
    @Length(max = MAX_PHONE_LENGTH, message = PARAM_ERROR)
    private String contactPhone;

    @Email(message = PARAM_ERROR)
    @Length(max = MAX_CONTACT_EMAIL_LENGTH, message = PARAM_ERROR)
    private String contactEmail;

    @Length(max = MAX_SHOP_ADDRESS_LENGTH, message = PARAM_ERROR)
    private String address;


}
