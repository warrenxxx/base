package org.micap.authentication.utils;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties
@PropertySource("classpath:application.oauth.properties")
public class OauthProperty {
    private String hostClientFront;
    private String hostClientBack;
    private Map<String,Oauth> oauthClient;
}
