package com.daxi.domain.ao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.daxi.limit.ChatLimit.MAX_MESSAGE_LENGTH;

@Data
public class SendMessageAO {
    @NotBlank
    @Size(max = MAX_MESSAGE_LENGTH)
    private String content;
}
