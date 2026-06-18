package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserPrivateAO implements ValidationConstants {
    public static final int MAX_PHONE_LENGTH = 20;
    public static final int MAX_EMAIL_LENGTH = 50;

    /**
     * 用户生日
     */
    private LocalDate birthday;

    /**
     * 绑定手机号（非必须）
     */
    @Size(max = MAX_PHONE_LENGTH, message = "手机号长度不能超过20")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 绑定邮箱（非必须）
     */
    @Size(max = MAX_EMAIL_LENGTH, message = "邮箱长度不能超过50")
    @Email(message = "邮箱格式不正确")
    private String email;
}
