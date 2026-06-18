package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class UserShopSkuSpecAO implements ValidationConstants {
    public static final int SPEC_VALUE_MIN_SORT=0;
    public static final int SPEC_VALUE_MAX_LENGTH=10;
    public static final int SPEC_VALUE_MAX_NUMBER=10;
    @Data
    public static class SpecAO {
        /** 规格名ID */
        @Min(value = MIN_ID, message = PARAM_ERROR)
        private Long nameId;
        /** 规格值ID
         * 没有就是新增
         * */
        @Min(value = MIN_ID, message = PARAM_ERROR)
        private Long valueId;
        /** 规格值 */
        @Length(max = SPEC_VALUE_MAX_LENGTH, message = PARAM_ERROR)
        private String specValue;
        @Min(value = SPEC_VALUE_MIN_SORT, message = PARAM_ERROR)
        /** 规格值排序权重（数字越小越靠前） */
        private Integer valueSort;
    }
    @Size(max = SPEC_VALUE_MAX_NUMBER, message = PARAM_ERROR)
    private List<SpecAO> specUpdates;
    @Size(max = SPEC_VALUE_MAX_NUMBER, message = PARAM_ERROR)
    private List<SpecAO> specAdds;
}
