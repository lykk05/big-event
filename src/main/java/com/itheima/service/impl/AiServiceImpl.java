package com.itheima.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.pojo.*;
import com.itheima.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${ai.api.url}")
    private String apiUrl;

    @Value("${ai.api.key}")
    private String apiKey;

    @Value("${ai.api.model:deepseek-chat}")
    private String aiModel;

    @Value("${ai.api.connect-timeout:10000}")
    private int connectTimeout;

    @Value("${ai.api.read-timeout:30000}")
    private int readTimeout;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 空内容标记
    private static final String EMPTY_CONTENT_MARKER = "<p><br></p>";

    @Override
    public AiResultVO generate(AiRequest request) {
        long startTime = System.currentTimeMillis();
        validateRequest(request);
        String userContent = buildUserContent(request);
        String systemPrompt = buildSystemPrompt(request.getAction());

        try {
            String aiRawResponse = callExternalAI(systemPrompt, userContent);
            AiOutputVO output = parseResponse(request.getAction(), aiRawResponse);
            long latencyMs = System.currentTimeMillis() - startTime;
            return buildResult(request, output, latencyMs);
        } catch (ResourceAccessException e) {
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw new RuntimeException("AI响应超时，请重试");
            }
            throw new RuntimeException("AI服务暂时不可用");
        } catch (Exception e) {
            log.error("AI调用异常", e);
            throw new RuntimeException("AI服务繁忙，请稍后重试");
        }
    }

    private void validateRequest(AiRequest request) {
        String content = request.getContent();
        if (content == null || content.isBlank() || EMPTY_CONTENT_MARKER.equals(content.trim())) {
            throw new RuntimeException("文章内容不能为空");
        }
        if (request.getAction() == AiAction.EXPAND && request.getTarget() != AiTarget.SELECTION) {
            throw new RuntimeException("扩写操作必须指定选中的文本范围");
        }
        if ((request.getAction() == AiAction.EXPAND || request.getAction() == AiAction.PROOFREAD)
                && request.getTarget() == AiTarget.SELECTION
                && (request.getSelectedText() == null || request.getSelectedText().isBlank())) {
            throw new RuntimeException("选中范围操作时，选中内容不能为空");
        }
        if (request.getAction() == AiAction.CUSTOM && (request.getInstruction() == null || request.getInstruction().isBlank())) {
            throw new RuntimeException("自定义指令不能为空");
        }
    }

    private String buildSystemPrompt(AiAction action) {
        return switch (action) {
            case GENERATE_TITLE -> "你是一个专业的文章标题生成助手。根据用户提供的文章内容，生成5个吸引人、简洁、符合中文阅读习惯的文章标题。" +
                    "你必须以严格的JSON数组格式返回，不要包含任何其他文字。格式示例：[\"标题1\",\"标题2\",\"标题3\",\"标题4\",\"标题5\"]";
            case RECOMMEND_CATEGORY -> "你是一个文章分类推荐助手。分析用户提供的文章内容或标题，从以下分类中选择最合适的一个：1-美食、2-娱乐、3-军事、4-旅游、5-科技、6-历史。" +
                    "你必须以严格的JSON对象格式返回，包含id(数字)、name(分类名称)、reason(推荐理由)。" +
                    "格式示例：{\"id\":4,\"name\":\"旅游\",\"reason\":\"文章主要介绍旅游相关内容\"}" ;
            case POLISH -> "你是一个专业的中文文本润色助手。对用户提供的文章进行润色优化，使语言更自然流畅、表达更精准有力。保持原文的事实和核心信息不变，只优化语言表达。返回润色后的完整HTML格式内容。";
            case CONTINUE -> "你是一个专业的写作续写助手。基于用户提供的文章内容和上下文，继续撰写后续段落。风格、语气和格式要与原文保持一致。返回续写部分的HTML格式内容（不含原文）。";
            case EXPAND -> "你是一个专业的文本扩写助手。将用户提供的内容进行详细扩展和丰富，增加细节描述、背景说明或案例分析。保持原有结构和核心观点不变。返回扩写后的完整HTML格式内容。";
            case PROOFREAD -> "你是一个专业的中文校对助手。检查用户提供的文本中的错别字、语法错误、标点问题等。" +
                    "你必须以严格的JSON格式返回，包含content(修正后的完整HTML文本)和corrections(纠错列表)。" +
                    "corrections每个元素格式：{\"original\":\"原文错误\",\"replacement\":\"修正后\",\"reason\":\"原因说明\"}" ;
            case CUSTOM -> "你是一个全能的写作助手。严格按照用户的自定义指令要求处理文章内容。";
        };
    }

    private String buildUserContent(AiRequest request) {
        StringBuilder sb = new StringBuilder();
        switch (request.getAction()) {
            case GENERATE_TITLE -> {
                sb.append("文章内容：\n").append(stripHtmlTags(request.getContent()));
                if (request.getTitle() != null && !request.getTitle().isBlank()) {
                    sb.append("\n原标题参考：").append(request.getTitle());
                }
            }
            case RECOMMEND_CATEGORY -> {
                if (request.getTitle() != null && !request.getTitle().isBlank()) {
                    sb.append("文章标题：").append(request.getTitle()).append("\n");
                }
                sb.append("文章内容摘要：\n").append(stripHtmlTags(request.getContent()));
            }
            case POLISH, CONTINUE -> {
                sb.append("文章标题：").append(request.getTitle() != null ? request.getTitle() : "未命名").append("\n");
                sb.append("文章内容：\n").append(request.getContent());
            }
            case EXPAND -> {
                sb.append("需要扩写的内容：\n").append(request.getSelectedText());
                sb.append("\n\n文章上下文：\n").append(stripHtmlTags(request.getContent()));
            }
            case PROOFREAD -> {
                if (request.getTarget() == AiTarget.SELECTION) {
                    sb.append("需要检查的选中内容：\n").append(request.getSelectedText());
                } else {
                    sb.append("需要检查的全文：\n").append(request.getContent());
                }
            }
            case CUSTOM -> {
                sb.append("用户指令：").append(request.getInstruction()).append("\n");
                sb.append("文章标题：").append(request.getTitle() != null ? request.getTitle() : "未命名").append("\n");
                sb.append("文章内容：\n").append(request.getContent());
            }
        }
        return sb.toString();
    }

    private String callExternalAI(String systemPrompt, String userContent) {
        RestTemplate restTemplate = createRestTemplateWithTimeout();
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

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI未返回有效结果");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }

    private RestTemplate createRestTemplateWithTimeout() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
            setConnectTimeout(connectTimeout);
            setReadTimeout(readTimeout);
        }});
        return restTemplate;
    }

    private AiOutputVO parseResponse(AiAction action, String rawResponse) throws JsonProcessingException {
        AiOutputVO output = new AiOutputVO();
        output.setContentType("HTML");
        String cleaned = cleanAiResponse(rawResponse);

        switch (action) {
            case GENERATE_TITLE -> {
                List<String> titles = parseJsonList(cleaned, new TypeReference<List<String>>() {});
                output.setTitles(titles != null ? titles : extractTitlesFromText(cleaned));
            }
            case RECOMMEND_CATEGORY -> {
                CategoryVO category = parseJsonObj(cleaned, CategoryVO.class);
                output.setCategory(category);
            }
            case POLISH, CONTINUE, EXPAND -> {
                output.setContent(cleaned);
            }
            case PROOFREAD -> {
                try {
                    Map<String, Object> proofMap = objectMapper.readValue(cleaned, new TypeReference<Map<String, Object>>() {});
                    output.setContent((String) proofMap.get("content"));
                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> corrList = (List<Map<String, String>>) proofMap.get("corrections");
                    if (corrList != null) {
                        output.setCorrections(corrList.stream()
                                .map(m -> new CorrectionVO(m.get("original"), m.get("replacement"), m.get("reason")))
                                .toList());
                    }
                } catch (Exception e) {
                    output.setContent(cleaned);
                    output.setCorrections(List.of());
                }
            }
            case CUSTOM -> {
                output.setContent(cleaned);
            }
        }
        return output;
    }

    private <T> T parseJsonObj(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.warn("JSON解析失败，尝试提取纯文本");
            return null;
        }
    }

    private <T> T parseJsonList(String json, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.warn("JSON列表解析失败");
            return null;
        }
    }

    private String cleanAiResponse(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            int start = cleaned.indexOf('\n');
            int end = cleaned.lastIndexOf("```");
            if (start > 0 && end > start) {
                cleaned = cleaned.substring(start + 1, end).trim();
            }
        }
        if (cleaned.startsWith("json") || cleaned.startsWith("JSON")) {
            cleaned = cleaned.substring(4).trim();
        }
        return cleaned;
    }

    private List<String> extractTitlesFromText(String text) {
        String[] lines = text.split("[\\n\\r]+");
        return java.util.Arrays.stream(lines)
                .map(String::trim)
                .filter(s -> s.length() > 2 && s.length() < 100)
                .limit(5)
                .toList();
    }

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private AiResultVO buildResult(AiRequest request, AiOutputVO output, long latencyMs) {
        return new AiResultVO(
                request.getAction().name(),
                output,
                new UsageVO(aiModel, 0, 0, latencyMs)
        );
    }
}