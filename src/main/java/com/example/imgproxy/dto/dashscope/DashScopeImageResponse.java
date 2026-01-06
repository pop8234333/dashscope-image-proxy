package com.example.imgproxy.dto.dashscope;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashScopeImageResponse {

    // DashScope 输出对象
    private Output output;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Output {
        // choices[0].message.content[0].image 为图片 URL
        private List<Choice> choices;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        // 单个候选消息
        private Message message;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        // message.content 是数组，元素包含 image 字段
        private List<Content> content;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        // 图片 URL（DashScope 返回的签名 OSS 地址）
        private String image;
    }
}
