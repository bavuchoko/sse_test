package com.example.sse_test.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;

@Configuration
public class JacksonConfiruration {

    @Bean
    public Jackson2HalModule halModule() {
        return new Jackson2HalModule();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(halModule());
        return objectMapper;
    }
}

