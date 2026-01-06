package com.example.imgproxy.dto.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OpenAiImageResponse {

    // Unix 时间戳（秒）
    private long created;
    private List<ImageData> data;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImageData {
        // Base64 编码的图片，不带 data:image/png;base64, 前缀
        @JsonProperty("b64_json")
        private String b64Json;
        // URL 形式（当前适配器不返回该字段）
        private String url;
    }
}
