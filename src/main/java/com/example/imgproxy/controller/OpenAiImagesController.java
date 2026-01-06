package com.example.imgproxy.controller;

import com.example.imgproxy.config.ProxyProperties;
import com.example.imgproxy.dto.openai.OpenAiImageRequest;
import com.example.imgproxy.dto.openai.OpenAiImageResponse;
import com.example.imgproxy.service.DashScopeImageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/images")
public class OpenAiImagesController {

    // 适配器配置（DashScope Key、是否校验外部 Key 等）
    private final ProxyProperties props;
    // 调用 DashScope 生成图片 URL 的服务
    private final DashScopeImageService dashScopeImageService;
    // 使用 JDK 自带 HttpClient 下载图片，避免 Open WebUI 再次去抓 URL
    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // OpenAI Images API 兼容入口：/v1/images/generations
    @PostMapping(value = "/generations", produces = MediaType.APPLICATION_JSON_VALUE)
    public OpenAiImageResponse generations(@Valid @RequestBody OpenAiImageRequest req,
                                           HttpServletRequest httpReq) {
        // 可选鉴权：外部调用者需携带 Authorization: Bearer <openai-key>
        if (props.isRequireOpenaiKey()) {
            String expected = props.getOpenaiKey();
            if (!StringUtils.hasText(expected)) {
                throw new IllegalStateException("OpenAI proxy key is missing");
            }

            String auth = httpReq.getHeader(HttpHeaders.AUTHORIZATION);
            String expectedHeader = "Bearer " + expected;
            if (!StringUtils.hasText(auth) || !auth.equals(expectedHeader)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
        }

        // 1) 调 DashScope 拿到图片 URL
        String imageUrl = dashScopeImageService.generateImageUrl(
                req.getPrompt(),
                req.getSize(),
                req.getModel()
        );

        // 2) 适配器自身下载图片并转 Base64，返回 b64_json，避免 WebUI 二次下载失败
        byte[] bytes = downloadImageBytes(imageUrl);
        String b64 = Base64.getEncoder().encodeToString(bytes);

        return OpenAiImageResponse.builder()
                .created(Instant.now().getEpochSecond())
                .data(List.of(OpenAiImageResponse.ImageData.builder().b64Json(b64).build()))
                .build();
    }

    // 下载图片二进制（支持 30x 重定向），失败直接抛异常
    private byte[] downloadImageBytes(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Accept", "image/*")
                    .GET()
                    .build();

            HttpResponse<byte[]> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            int code = resp.statusCode();
            if (code / 100 != 2) {
                throw new IllegalStateException("Download image failed: HTTP " + code);
            }
            return resp.body();
        } catch (Exception e) {
            throw new IllegalStateException("Download image failed", e);
        }
    }
}
