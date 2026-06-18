package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class UserShopCreateAO implements ValidationConstants {
    @NotBlank
    @Size(max = SHOP_NAME_MAX_LENGTH)
    private String shopName;

    @NotNull
    private Integer shopType;

    @Size(max = MAX_URL_LENGTH)
    private String logoUrl;

    @Size(max = MAX_SHOP_DESC_LENGTH)
    private String shopDesc;

    @Size(max = MAX_BUSINESS_HOURS_LENGTH)
    private String businessHours;

    @Size(max = MAX_PHONE_LENGTH)
    private String contactPhone;

    @Size(max = MAX_EMAIL_LENGTH)
    private String contactEmail;

    @Size(max = MAX_DETAIL_ADDRESS_LENGTH)
    private String address;
}
