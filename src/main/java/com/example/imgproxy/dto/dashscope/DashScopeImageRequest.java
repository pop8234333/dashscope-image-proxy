package com.example.imgproxy.dto.dashscope;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashScopeImageRequest {
    private String model;
    private Map<String, Object> input;
    private Map<String, Object> parameters;

    public static DashScopeImageRequest of(String model, String promptText, String size,
                                           boolean promptExtend, boolean watermark) {
        Map<String, Object> content = Map.<String, Object>of("text", promptText);
        Map<String, Object> message = Map.<String, Object>of(
                "role", "user",
                "content", List.<Map<String, Object>>of(content)
        );

        Map<String, Object> input = Map.<String, Object>of(
                "messages", List.<Map<String, Object>>of(message)
        );

        Map<String, Object> parameters = Map.<String, Object>of(
                "size", size,
                "n", 1,
                "prompt_extend", promptExtend,
                "watermark", watermark
        );

        return DashScopeImageRequest.builder()
                .model(model)
                .input(input)
                .parameters(parameters)
                .build();
    }
}
