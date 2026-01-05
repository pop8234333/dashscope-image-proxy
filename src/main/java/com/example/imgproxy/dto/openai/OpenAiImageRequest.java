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

    private String model;

    @NotBlank
    private String prompt;

    private Integer n;

    private String size;

    @JsonProperty("response_format")
    private String responseFormat;
}
