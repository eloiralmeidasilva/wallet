package com.eloir.wallet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "config-params")
public class ConfigParams {

    private String[] openedEndpointRoutes;
}