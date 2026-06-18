package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import static com.daxi.response.CommonResponse.ID_ERROR;
import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class LogisticsCenterAO implements ValidationConstants {
    @NotNull(message = ID_ERROR)
    @Min(value = MIN_ID, message = ID_ERROR)
    private Long refundId;

    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_LOGISTICS_COMPANY_LENGTH, message = PARAM_ERROR)
    private String returnExpressCompany;

    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_WAYBILL_LENGTH, message = PARAM_ERROR)
    private String returnWaybill;
}
