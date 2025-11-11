package com.finx.strategyengineservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson Configuration Class
 * @author Naveen Manyam
 * @version 1.0
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mapper.coercionConfigFor(java.util.Map.class)
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);
        mapper.coercionConfigFor(Object.class)
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);

        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING);
        mapper.deactivateDefaultTyping();

        return mapper;
    }
}
