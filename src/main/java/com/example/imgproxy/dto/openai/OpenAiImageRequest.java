package com.example.imgproxy.dto.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAiImageRequest {

    // OpenAI 风格模型名（适配器可选映射到 DashScope 模型）
    private String model;

    // 文生图提示词
    @NotBlank
    private String prompt;

    // 期望生成数量（DashScope 同步接口实际只支持 1）
    private Integer n;

    // OpenAI 风格尺寸，如 1024x1024
    private String size;

    // OpenAI 输出格式：url 或 b64_json（当前适配器统一返回 b64_json）
    @JsonProperty("response_format")
    private String responseFormat;
}
