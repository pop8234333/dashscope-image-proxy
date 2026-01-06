package com.example.imgproxy.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "proxy")
public class ProxyProperties {

    // DashScope 域名（北京/新加坡二选一）
    private String dashscopeBaseUrl;
    // DashScope API Key（建议用环境变量注入）
    private String dashscopeApiKey;
    // 默认模型
    private String dashscopeModel = "qwen-image-max";

    // 是否要求调用方携带 Authorization: Bearer <openai-key>
    private boolean requireOpenaiKey = false;
    // 适配器对外鉴权 key
    private String openaiKey;

    // 是否启用 prompt 智能改写
    private boolean promptExtend = false;
    // 是否加水印
    private boolean watermark = false;
}
