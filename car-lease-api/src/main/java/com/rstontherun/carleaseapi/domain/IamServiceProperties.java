package com.rstontherun.carleaseapi.domain;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;


@Data
@Builder
@Slf4j
@ConfigurationProperties(prefix="iam-service")
public class IamServiceProperties {
    private String url;
    private String clientId;
    private String clientSecret;
    private String introspectResource;
    private String loginResource;
}
