package com.daxi.domain.ao;


import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.daxi.response.CommonResponse.ID_ERROR;
import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class AddressModifyAuditAO implements ValidationConstants {
    @NotNull(message = ID_ERROR)
    @Min(value = MIN_ID, message = ID_ERROR)
    private Long orderId;

    //1同意2拒绝
    @NotNull(message = PARAM_ERROR)
    @Min(value = AUDIT_RESULT_AGREE, message = PARAM_ERROR)
    @Max(value = AUDIT_RESULT_REJECT, message = PARAM_ERROR)
    private Integer auditResult;}
