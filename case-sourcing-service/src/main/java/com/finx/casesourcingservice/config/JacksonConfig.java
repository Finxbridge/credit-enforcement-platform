package com.finx.casesourcingservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Disable writing of type information (e.g., "@class" field)
        // This is the primary change to remove the @class field
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS); // Example of another common config
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Example for date serialization

        // Explicitly disable default typing if it was somehow enabled
        // This is a more robust way to ensure it's off
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .build();
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.EVERYTHING); // Temporarily activate to then disable
        mapper.deactivateDefaultTyping(); // Deactivate default typing

        return mapper;
    }
}
