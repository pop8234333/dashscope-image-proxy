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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/images")
public class OpenAiImagesController {

    private final ProxyProperties props;
    private final DashScopeImageService dashScopeImageService;

    @PostMapping("/generations")
    public OpenAiImageResponse generations(@Valid @RequestBody OpenAiImageRequest req,
                                           HttpServletRequest httpReq) {
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

        String imageUrl = dashScopeImageService.generateImageUrl(
                req.getPrompt(),
                req.getSize(),
                req.getModel()
        );

        return OpenAiImageResponse.builder()
                .created(Instant.now().getEpochSecond())
                .data(List.of(OpenAiImageResponse.ImageData.builder().url(imageUrl).build()))
                .build();
    }
}
