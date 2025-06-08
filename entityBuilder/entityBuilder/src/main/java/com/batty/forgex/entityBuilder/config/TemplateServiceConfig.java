package com.batty.forgex.entitybuilder.config;

import com.batty.forgex.template.client.api.DefaultApi;
import com.batty.forgex.template.client.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemplateServiceConfig {
    @Value("${template.service.url:http://localhost:8080}")
    private String templateServiceUrl;

    @Bean
    public ApiClient templateApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(templateServiceUrl);
        return apiClient;
    }

    @Bean
    public DefaultApi templateApi(ApiClient apiClient) {
        return new DefaultApi(apiClient);
    }
} 