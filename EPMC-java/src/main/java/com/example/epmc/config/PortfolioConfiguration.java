package com.example.epmc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "portfolio")
@Data
public class PortfolioConfiguration {
    private int maxGrants = 10;
    private int maxConcurrentLookups = 10;
    private int perCallTimeoutSeconds = 5;
    private int overallTimeoutSeconds = 30;
    private int maxPublicationsPerGrant = 5;
}
