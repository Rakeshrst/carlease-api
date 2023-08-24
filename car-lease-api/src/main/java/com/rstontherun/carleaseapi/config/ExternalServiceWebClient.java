package com.rstontherun.carleaseapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExternalServiceWebClient {

    @Bean
    WebClient iamWebClient(@Value("${iam-service.url}") String iamUrl) {
        return createWebClient(iamUrl);
    }

    private WebClient createWebClient(String baseurl) {
        return WebClient.builder()
                .baseUrl(baseurl)
                .build();
    }
}
