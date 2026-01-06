package com.example.imgproxy.service;

import com.example.imgproxy.config.ProxyProperties;
import com.example.imgproxy.dto.dashscope.DashScopeImageRequest;
import com.example.imgproxy.dto.dashscope.DashScopeImageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashScopeImageService {

    // 允许从 OpenAI 请求中覆盖的模型名（其余情况使用配置默认模型）
    private static final Set<String> OVERRIDE_MODELS = Set.of(
            "qwen-image-max",
            "qwen-image-plus",
            "qwen-image"
    );

    // 适配器配置（域名、Key、默认模型、参数开关）
    private final ProxyProperties props;
    // Spring 6 的轻量 HTTP 客户端
    private final RestClient restClient = RestClient.builder().build();

    // 将 OpenAI 风格请求转换为 DashScope 请求，返回图片 URL
    public String generateImageUrl(String prompt, String openAiSize, String openAiModel) {
        if (!StringUtils.hasText(props.getDashscopeApiKey())) {
            throw new IllegalStateException("DashScope API key is missing");
        }

        // OpenAI size → DashScope size 映射
        String dashscopeSize = mapSize(openAiSize);
        // OpenAI model → DashScope model 映射
        String dashscopeModel = pickModel(openAiModel);

        DashScopeImageRequest req = DashScopeImageRequest.of(
                dashscopeModel,
                prompt,
                dashscopeSize,
                props.isPromptExtend(),
                props.isWatermark()
        );

        // 统一拼接接口路径，避免配置末尾 "/" 导致双斜杠
        String baseUrl = props.getDashscopeBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String url = baseUrl + "/api/v1/services/aigc/multimodal-generation/generation";

        DashScopeImageResponse resp = restClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + props.getDashscopeApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(req)
                .retrieve()
                .body(DashScopeImageResponse.class);

        // 从 DashScope 响应中提取图片 URL
        String imageUrl = extractImageUrl(resp);
        log.info("DashScope image generated. model={}, size={}, url={}", dashscopeModel, dashscopeSize, imageUrl);
        return imageUrl;
    }

    // 将 OpenAI 的 512x512/1024x1024 等尺寸映射到 DashScope 固定 5 档
    private String mapSize(String openAiSize) {
        if (!StringUtils.hasText(openAiSize)) {
            return "1664*928";
        }

        String[] parts = openAiSize.toLowerCase().split("x");
        if (parts.length != 2) {
            return "1664*928";
        }

        int w;
        int h;
        try {
            w = Integer.parseInt(parts[0].trim());
            h = Integer.parseInt(parts[1].trim());
        } catch (Exception e) {
            return "1664*928";
        }

        if (w == h) {
            return "1328*1328";
        }

        double ratio = (double) w / (double) h;
        if (ratio > 1.0) {
            // 横图：选更接近的 16:9 或 4:3
            return (Math.abs(ratio - (16.0 / 9.0)) <= Math.abs(ratio - (4.0 / 3.0)))
                    ? "1664*928"
                    : "1472*1104";
        }

        // 竖图：选更接近的 9:16 或 3:4
        return (Math.abs(ratio - (9.0 / 16.0)) <= Math.abs(ratio - (3.0 / 4.0)))
                ? "928*1664"
                : "1104*1472";
    }

    // 简单模型映射：若外部 model 在允许列表中则使用，否则用配置默认
    private String pickModel(String openAiModel) {
        if (StringUtils.hasText(openAiModel)) {
            String normalized = openAiModel.trim().toLowerCase();
            if (OVERRIDE_MODELS.contains(normalized)) {
                return normalized;
            }
        }
        return props.getDashscopeModel();
    }

    // 防御式解析 DashScope 响应结构，缺字段直接抛错
    private String extractImageUrl(DashScopeImageResponse resp) {
        if (resp == null || resp.getOutput() == null || resp.getOutput().getChoices() == null
                || resp.getOutput().getChoices().isEmpty()
                || resp.getOutput().getChoices().get(0).getMessage() == null
                || resp.getOutput().getChoices().get(0).getMessage().getContent() == null
                || resp.getOutput().getChoices().get(0).getMessage().getContent().isEmpty()
                || resp.getOutput().getChoices().get(0).getMessage().getContent().get(0).getImage() == null) {
            throw new IllegalStateException("DashScope response missing image URL");
        }
        return resp.getOutput().getChoices().get(0).getMessage().getContent().get(0).getImage();
    }
}
