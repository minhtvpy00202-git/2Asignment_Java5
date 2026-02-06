package com.poly.ASM.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
@EnableConfigurationProperties(PayosProperties.class)
public class PayosConfig {

    @Bean
    public PayOS payOS(PayosProperties properties) {
        return new PayOS(properties.getClientId(), properties.getApiKey(), properties.getChecksumKey());
    }
}
