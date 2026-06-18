package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.daxi.response.UserResponse.MAX_USER_NAME_ERROR;

@Data
public class UserDisplayAO implements ValidationConstants {
    public static final int MIN_GENDER = 0;
    public static final int MAX_GENDER = 2;

    /**
     * 用户昵称
     */
    @Size(max = MAX_NAME_LENGTH, message = MAX_USER_NAME_ERROR)
    private String nickname;

    /**
     * 性别：0-未知 1-男 2-女
     */
    @Min(value = MIN_GENDER, message = "性别参数错误")
    @Max(value = MAX_GENDER, message = "性别参数错误")
    private Integer gender;

    /**
     * 用户头像URL
     */
    private String avatarUrl;
}
