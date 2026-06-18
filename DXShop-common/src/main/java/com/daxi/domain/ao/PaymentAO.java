package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class PaymentAO implements ValidationConstants {
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long orderId;

    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private String paySn;
    @NotNull(message = PARAM_ERROR)
    private LocalDateTime payTime;
}
