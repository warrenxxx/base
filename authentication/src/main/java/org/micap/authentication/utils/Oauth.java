package org.micap.authentication.utils;

import lombok.Data;

@Data
public class Oauth{
    private String clientId;
    private String secret;
    private String urlCallback;
}
