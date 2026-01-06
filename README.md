# DashScope Image Proxy

## 项目作用
这是一个 **OpenAI Images API 兼容适配器**。对外提供 `POST /v1/images/generations`，
内部调用 DashScope（百炼）Qwen-Image 同步接口，**再由适配器主动下载图片并返回 `b64_json`**。
这样可以绕过 Open WebUI 对图片 URL 下载与 Content-Type 的依赖，提高稳定性。

## 主要特性
- OpenAI Images API 兼容：可直接接入 Open WebUI 的 image engine（openai）
- DashScope 适配：自动映射尺寸到 DashScope 支持的 5 个固定档位
- 统一返回 `b64_json`：避免 Open WebUI 二次下载图片失败
- 可选鉴权：支持 `Authorization: Bearer <openai-key>`

## 目录结构（核心）
- `src/main/java/com/example/imgproxy/controller/OpenAiImagesController.java`：OpenAI 兼容入口
- `src/main/java/com/example/imgproxy/service/DashScopeImageService.java`：DashScope 调用与尺寸映射
- `src/main/resources/application.yml`：默认配置
- `Dockerfile` / `docker-compose.yml`：容器化与联动部署

## 配置说明
application.yml（可用环境变量覆盖）：
- `proxy.dashscope-base-url`：DashScope 域名（北京/新加坡二选一）
- `proxy.dashscope-api-key`：DashScope API Key（建议用环境变量 `DASHSCOPE_API_KEY` 注入）
- `proxy.dashscope-model`：默认模型（如 `qwen-image-max`）
- `proxy.require-openai-key`：是否启用外部调用鉴权
- `proxy.openai-key`：外部调用鉴权 key
- `proxy.prompt-extend`：是否启用 prompt 自动润色
- `proxy.watermark`：是否添加水印

常用环境变量示例：
- `DASHSCOPE_API_KEY`：DashScope API Key
- `PROXY_DASHSCOPE_BASE_URL`：DashScope 域名
- `PROXY_DASHSCOPE_MODEL`：模型名
- `PROXY_REQUIRE_OPENAI_KEY`：是否校验外部 key（true/false）
- `PROXY_OPENAI_KEY`：外部 key
- `PROXY_PROMPT_EXTEND`、`PROXY_WATERMARK`

## 本地运行（JDK 21 + Maven）
```bash
# Windows PowerShell
$env:DASHSCOPE_API_KEY="你的DashScopeKey"
mvn -q -DskipTests spring-boot:run
```

测试接口：
```bash
curl -sS http://localhost:8081/v1/images/generations ^
  -H "Content-Type: application/json" ^
  -d "{\"model\":\"dashscope-image\",\"prompt\":\"test\",\"size\":\"1024x1024\",\"n\":1}"
```

返回示例（截断）：
```json
{
  "created": 1736xxxxxx,
  "data": [
    { "b64_json": "iVBORw0K..." }
  ]
}
```

## Docker 构建与运行
构建镜像：
```bash
docker build -t dashscope-image-adapter:1.0 .
```

运行适配器容器：
```bash
docker run -d --name dashscope-image-adapter ^
  -p 8081:8081 ^
  -e DASHSCOPE_API_KEY="你的DashScopeKey" ^
  -e PROXY_DASHSCOPE_BASE_URL="https://dashscope.aliyuncs.com" ^
  -e PROXY_DASHSCOPE_MODEL="qwen-image-max" ^
  -e PROXY_PROMPT_EXTEND="false" ^
  -e PROXY_WATERMARK="false" ^
  dashscope-image-adapter:1.0
```

## 与 Open WebUI 联动（docker-compose）
本项目已提供 `docker-compose.yml`，将适配器与 Open WebUI 放在同一网络中。

启动前设置：
```bash
export DASHSCOPE_API_KEY="你的DashScopeKey"
```

启动：
```bash
docker compose up -d --build
```

关键环境变量（Open WebUI）：
- `IMAGE_GENERATION_ENGINE=openai`
- `IMAGES_OPENAI_API_BASE_URL=http://dashscope-image-adapter:8081/v1`
- `IMAGES_OPENAI_API_KEY=dummy`（适配器未启用鉴权时任意值即可）
- `IMAGE_GENERATION_MODEL=dashscope-image`
- `IMAGE_GENERATION_OUTPUT_FORMAT=url` **可保留为 url，但适配器会返回 b64_json**

如果你曾在 WebUI 管理界面改过图像配置，可能会被持久化配置覆盖。
可临时设置 `ENABLE_PERSISTENT_CONFIG=false` 强制使用环境变量。

## 常见问题排查
1. `no main manifest attribute`
   - 说明 Jar 非可执行包。已通过 `spring-boot-maven-plugin repackage` 解决，请重新构建镜像。
2. `Could not resolve host: dashscope-image-adapter`
   - 说明适配器容器未成功运行或不在同一网络，先确保适配器容器健康并在同一 compose 网络。
3. Open WebUI 报 `NoneType has no attribute 'lower'`
   - 该问题常由 Open WebUI 内部处理 URL 失败引起。当前适配器已返回 `b64_json` 避免再次下载。
4. `DashScope API key is missing`
   - 未配置 `DASHSCOPE_API_KEY` 环境变量或 `proxy.dashscope-api-key`。

## 接口约定
OpenAI Images API 入口：
- `POST /v1/images/generations`
- 入参：`{ model, prompt, n, size, response_format }`
- 出参：`{ created, data: [ { b64_json } ] }`

DashScope 端限制：
- `n` 固定为 1
- `size` 仅支持 5 个固定尺寸（适配器已自动映射）
