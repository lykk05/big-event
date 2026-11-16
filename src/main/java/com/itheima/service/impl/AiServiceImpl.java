package com.itheima.service.impl;

import com.itheima.exception.AiException;
import com.itheima.exception.BizException;
import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;
import com.itheima.pojo.AiResultVO;
import com.itheima.pojo.AiTarget;
import com.itheima.pojo.UsageVO;
import com.itheima.service.AiService;
import com.itheima.service.ai.AiActionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    // 空内容标记
    private static final String EMPTY_CONTENT_MARKER = "<p><br></p>";

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final Map<AiAction, AiActionHandler> handlers;

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.model:deepseek-chat}")
    private String aiModel;

    @Value("${ai.rate-limit.daily:50}")
    private long dailyLimit;

    public AiServiceImpl(RestTemplate aiRestTemplate,
                         StringRedisTemplate redisTemplate,
                         List<AiActionHandler> handlerList) {
        this.restTemplate = aiRestTemplate;
        this.redisTemplate = redisTemplate;
        this.handlers = handlerList.stream()
                .collect(Collectors.toMap(AiActionHandler::action, handler -> handler));
    }

    @Override
    public AiResultVO generate(AiRequest request, Integer userId) {
        long startTime = System.currentTimeMillis();
        validateRequest(request);
        checkRateLimit(userId);

        AiActionHandler handler = handlers.get(request.getAction());
        if (handler == null) {
            throw new BizException(400, "不支持的操作类型");
        }

        try {
            AiCallResult callResult = callExternalAI(handler.systemPrompt(request), handler.userContent(request));
            AiOutputVO output = handler.parse(callResult.content());
            long latencyMs = System.currentTimeMillis() - startTime;

            log.info("AI call action={} model={} inputTokens={} outputTokens={} latencyMs={} userId={}",
                    request.getAction(), aiModel, callResult.promptTokens(), callResult.completionTokens(),
                    latencyMs, userId);

            UsageVO usage = new UsageVO(aiModel, callResult.promptTokens(), callResult.completionTokens(), latencyMs);
            return new AiResultVO(request.getAction().name(), output, usage);
        } catch (AiException | BizException e) {
            throw e;
        } catch (ResourceAccessException e) {
            Throwable root = e.getMostSpecificCause();
            if (root instanceof SocketTimeoutException) {
                throw new AiException("AI_TIMEOUT", "AI响应超时，请重试", e);
            }
            throw new AiException("AI_UNAVAILABLE", "AI服务暂时不可用", e);
        } catch (HttpStatusCodeException e) {
            // 401 / 429 / 5xx
            throw new AiException("AI_HTTP_" + e.getStatusCode().value(), "AI服务返回异常", e);
        } catch (Exception e) {
            log.error("AI调用异常", e);
            throw new AiException("AI_ERROR", "AI服务繁忙，请稍后重试", e);
        }
    }

    private void validateRequest(AiRequest request) {
        if (request == null || request.getAction() == null) {
            throw new BizException(400, "操作类型不能为空");
        }
        String content = request.getContent();
        if (content == null || content.isBlank() || EMPTY_CONTENT_MARKER.equals(content.trim())) {
            throw new BizException(400, "文章内容不能为空");
        }
        if (request.getAction() == AiAction.EXPAND && request.getTarget() != AiTarget.SELECTION) {
            throw new BizException(400, "扩写操作必须指定选中的文本范围");
        }
        if ((request.getAction() == AiAction.EXPAND || request.getAction() == AiAction.PROOFREAD)
                && request.getTarget() == AiTarget.SELECTION
                && (request.getSelectedText() == null || request.getSelectedText().isBlank())) {
            throw new BizException(400, "选中范围操作时，选中内容不能为空");
        }
        if (request.getAction() == AiAction.CUSTOM
                && (request.getInstruction() == null || request.getInstruction().isBlank())) {
            throw new BizException(400, "自定义指令不能为空");
        }
    }

    // 按用户每日限流，避免单个账号刷光 AI 额度
    private void checkRateLimit(Integer userId) {
        if (userId == null) {
            throw new BizException(401, "未登录");
        }
        String key = "ai:rate:" + userId + ":" + LocalDate.now();
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }
        if (count != null && count > dailyLimit) {
            throw new BizException(429, "今日 AI 调用次数已达上限，请明天再试");
        }
    }

    @SuppressWarnings("unchecked")
    private AiCallResult callExternalAI(String systemPrompt, String userContent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", aiModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userContent)
                ),
                "temperature", 0.7,
                "max_tokens", 4096
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, new HttpEntity<>(body, headers), Map.class);
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new AiException("AI_EMPTY", "AI未返回有效结果");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new AiException("AI_EMPTY", "AI未返回有效结果");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        Object contentObject = message == null ? null : message.get("content");
        if (!(contentObject instanceof String content) || content.isBlank()) {
            throw new AiException("AI_EMPTY", "AI未返回有效内容");
        }

        Map<String, Object> usage = (Map<String, Object>) responseBody.get("usage");
        int promptTokens = readTokenCount(usage, "prompt_tokens");
        int completionTokens = readTokenCount(usage, "completion_tokens");
        return new AiCallResult(content, promptTokens, completionTokens);
    }

    private int readTokenCount(Map<String, Object> usage, String key) {
        if (usage == null) {
            return 0;
        }
        Object value = usage.get(key);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private record AiCallResult(String content, int promptTokens, int completionTokens) {
    }
}
