package com.itheima.service.impl;

import com.itheima.exception.AiException;
import com.itheima.exception.BizException;
import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;
import com.itheima.pojo.AiResultVO;
import com.itheima.pojo.AiTarget;
import com.itheima.service.ai.AiActionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private AiActionHandler handler;

    private AiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        when(handler.action()).thenReturn(AiAction.POLISH);
        aiService = new AiServiceImpl(restTemplate, redisTemplate, List.of(handler));
        ReflectionTestUtils.setField(aiService, "apiUrl", "http://localhost/ai");
        ReflectionTestUtils.setField(aiService, "apiKey", "test-key");
        ReflectionTestUtils.setField(aiService, "aiModel", "deepseek-test");
        ReflectionTestUtils.setField(aiService, "dailyLimit", 50L);
    }

    @Test
    void rateLimitRejectsWhenDailyLimitExceeded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(51L);

        BizException ex = assertThrows(BizException.class, () -> aiService.generate(request(), 1));

        assertEquals(429, ex.getCode());
        verifyNoInteractions(restTemplate);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void generateParsesUsage() throws Exception {
        stubRateLimit();
        when(handler.systemPrompt(any())).thenReturn("system");
        when(handler.userContent(any())).thenReturn("user");
        AiOutputVO output = new AiOutputVO();
        output.setContent("<p>x</p>");
        when(handler.parse(anyString())).thenReturn(output);

        Map<String, Object> body = Map.of(
                "choices", List.of(Map.of("message", Map.of("content", "<p>x</p>"))),
                "usage", Map.of("prompt_tokens", 12, "completion_tokens", 34)
        );
        ResponseEntity<Map> response = ResponseEntity.ok(body);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(response);

        AiResultVO result = aiService.generate(request(), 1);

        assertEquals("<p>x</p>", result.getOutput().getContent());
        assertEquals(12, result.getUsage().getInputTokens());
        assertEquals(34, result.getUsage().getOutputTokens());
        assertEquals("deepseek-test", result.getUsage().getModel());
    }

    @Test
    void generateMapsSocketTimeoutToAiTimeout() throws Exception {
        stubRateLimit();
        when(handler.systemPrompt(any())).thenReturn("system");
        when(handler.userContent(any())).thenReturn("user");
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenThrow(new ResourceAccessException("timeout", new SocketTimeoutException("read timed out")));

        AiException ex = assertThrows(AiException.class, () -> aiService.generate(request(), 1));

        assertEquals("AI_TIMEOUT", ex.getCode());
    }

    private void stubRateLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);
    }

    private AiRequest request() {
        AiRequest request = new AiRequest();
        request.setAction(AiAction.POLISH);
        request.setTarget(AiTarget.FULL_CONTENT);
        request.setContent("hello");
        return request;
    }
}
