package com.poly.ASM.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "payos")
public class PayosProperties {
    private String clientId;
    private String apiKey;
    private String checksumKey;
}
