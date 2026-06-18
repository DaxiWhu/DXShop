package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class UserShopSpecNameAO implements ValidationConstants {
    @NotBlank(message = PARAM_ERROR)
    @Length(max = MAX_SPEC_NAME_LENGTH, message = PARAM_ERROR)
    String specName;

    @Min(value = MIN_SORT, message = PARAM_ERROR)
    @NotNull(message = PARAM_ERROR)
    Integer sort;
}
