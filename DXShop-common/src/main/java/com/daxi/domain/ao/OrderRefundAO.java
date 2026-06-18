package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class OrderRefundAO implements ValidationConstants {

    /**
     * 订单ID
     */
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long orderId;

    /**
     * 退款类型：1-仅退款 2-退货退款
     */
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_REFUND_TYPE, message = PARAM_ERROR)
    @Max(value = MAX_REFUND_TYPE, message = PARAM_ERROR)
    private Integer refundType;

    /**
     * 退款原因
     */
    @Length(max = MAX_REFUND_REASON_LENGTH, message = PARAM_ERROR)
    private String refundReason;

    /**
     * 凭证图片URL列表（JSON格式）
     */
    @Length(max = MAX_URL_LENGTH, message = PARAM_ERROR)
    private String evidenceImages;
}
