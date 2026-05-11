package com.atl.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@PropertySource("classpath:config/stripe.properties")
@ConfigurationProperties(prefix = "stripe")
public class StripeConfig {
    private String secretKey;
    private String publishableKey;
    private String apiVersion;
}
