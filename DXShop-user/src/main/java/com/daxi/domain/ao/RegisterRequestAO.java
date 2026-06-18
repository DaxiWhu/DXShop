package com.daxi.domain.ao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class RegisterRequestAO {

    @NotBlank(message = PARAM_ERROR)
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = PARAM_ERROR)
    private String phone;

    @NotBlank(message = PARAM_ERROR)
    @Pattern(regexp = "^.{6,20}$", message = PARAM_ERROR)
    private String password;

    @NotBlank(message = PARAM_ERROR)
    @Pattern(regexp = "^\\d{6}$", message = PARAM_ERROR)
    private String verifyCode;
}
