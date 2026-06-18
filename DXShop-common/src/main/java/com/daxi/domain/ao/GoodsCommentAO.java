package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class GoodsCommentAO implements ValidationConstants {
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long spuId;

    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long skuId;

    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long orderId;

    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_COMMENT_LENGTH, message = PARAM_ERROR)
    private String content;

    private Integer score;

    private Integer isAnonymous;

    @Size(max = MAX_URL_LENGTH, message = PARAM_ERROR)
    private List<String> pictures;

}
