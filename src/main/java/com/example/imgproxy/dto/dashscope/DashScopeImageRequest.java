package com.example.imgproxy.dto.dashscope;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashScopeImageRequest {
    // DashScope 模型名
    private String model;
    // input.messages 结构（按 DashScope 文档）
    private Map<String, Object> input;
    // parameters：size、n、prompt_extend、watermark 等
    private Map<String, Object> parameters;

    public static DashScopeImageRequest of(String model, String promptText, String size,
                                           boolean promptExtend, boolean watermark) {
        // content 里的 text 为提示词
        Map<String, Object> content = Map.<String, Object>of("text", promptText);
        // 单条 user 消息
        Map<String, Object> message = Map.<String, Object>of(
                "role", "user",
                "content", List.<Map<String, Object>>of(content)
        );

        // input.messages 数组
        Map<String, Object> input = Map.<String, Object>of(
                "messages", List.<Map<String, Object>>of(message)
        );

        // DashScope 同步接口 n 固定为 1
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
