package com.daxi.domain.ao;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentLoginAO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
