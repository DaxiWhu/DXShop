package com.daxi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule simpleModule = new SimpleModule();

        // 只序列化包装类型 Long（通常是 ID）
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        // 不处理基本类型 long（通常是时间戳、计数等）

        objectMapper.registerModule(simpleModule);
        return objectMapper;
    }
}
