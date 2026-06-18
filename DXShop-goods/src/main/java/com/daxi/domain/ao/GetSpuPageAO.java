package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class GetSpuPageAO implements ValidationConstants {
    @Min(value = MIN_PAGE_NUM, message = PARAM_ERROR)
    @Max(value = MAX_PAGE_NUM, message = PARAM_ERROR)
    @NotNull(message = PARAM_ERROR)
    private Integer pageNum;
    @Min(value = MIN_PAGE_SIZE, message = PARAM_ERROR)
    @Max(value = MAX_SPU_PAGE_SIZE, message = PARAM_ERROR)
    @NotNull(message = PARAM_ERROR)
    private Integer pageSize;
}
