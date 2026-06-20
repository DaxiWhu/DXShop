package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class GetPendingSessionsAO implements ValidationConstants {
    private HttpServletRequest request;
    @Min(value = MIN_PAGE_NUM, message = PARAM_ERROR)
    @Max(value = MAX_PAGE_NUM, message = PARAM_ERROR)
    private int page;
    @Min(value = MIN_PAGE_SIZE, message = PARAM_ERROR)
    @Max(value = MAX_PAGE_SIZE, message = PARAM_ERROR)
    private int size;
}
