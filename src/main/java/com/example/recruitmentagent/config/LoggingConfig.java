package com.example.recruitmentagent.config;

import jakarta.servlet.Filter;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class LoggingConfig {

    @Bean
    public Filter traceIdFilter() {
        return (request, response, chain) -> {
            try {
                String traceId = UUID.randomUUID().toString().substring(0, 8);
                MDC.put("traceId", traceId);
                chain.doFilter(request, response);
            } finally {
                MDC.clear();
            }
        };
    }
}
