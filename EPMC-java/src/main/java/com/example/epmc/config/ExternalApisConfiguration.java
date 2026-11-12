package com.example.epmc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "external-apis")
@Data
public class ExternalApisConfiguration {
    private String gristApiBaseUrl = "https://api.grist.example.com";
    private String articlesApiBaseUrl = "https://api.europepmc.org";
    private int connectionTimeoutSeconds = 5;
}
