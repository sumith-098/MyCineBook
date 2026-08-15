package com.cinebook.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient authServiceRestClient(@Value("${app.services.auth-service-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient catalogServiceRestClient(@Value("${app.services.catalog-service-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public RestClient bookingServiceRestClient(@Value("${app.services.booking-service-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
