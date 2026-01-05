package com.example.imgproxy.dto.openai;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OpenAiImageResponse {

    private long created;
    private List<ImageData> data;

    @Getter
    @Builder
    public static class ImageData {
        private String url;
    }
}
