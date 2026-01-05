package com.example.imgproxy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "proxy")
public class ProxyProperties {

    private String dashscopeBaseUrl;
    private String dashscopeApiKey;
    private String dashscopeModel = "qwen-image-max";

    private boolean requireOpenaiKey = false;
    private String openaiKey;

    private boolean promptExtend = false;
    private boolean watermark = false;
}
