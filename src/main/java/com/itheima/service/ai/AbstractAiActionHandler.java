package com.itheima.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.pojo.AiOutputVO;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 动作处理器的公共解析工具。
 */
@Slf4j
public abstract class AbstractAiActionHandler implements AiActionHandler {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected String cleanAiResponse(String raw) {
        if (raw == null) {
            return "";
        }
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

    protected String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    protected <T> T parseJsonObj(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.warn("AI JSON 解析失败，raw={}", abbreviate(json));
            return null;
        }
    }

    protected <T> T parseJsonList(String json, TypeReference<T> typeRef) {
        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            log.warn("AI JSON 列表解析失败，raw={}", abbreviate(json));
            return null;
        }
    }

    protected List<String> extractTitlesFromText(String text) {
        String[] lines = text.split("[\\n\\r]+");
        return Arrays.stream(lines)
                .map(String::trim)
                .filter(s -> s.length() > 2 && s.length() < 100)
                .limit(5)
                .toList();
    }

    protected AiOutputVO contentOutput(String rawResponse) {
        AiOutputVO output = new AiOutputVO();
        output.setContentType("HTML");
        output.setContent(cleanAiResponse(rawResponse));
        return output;
    }

    protected String abbreviate(String raw) {
        if (raw == null) {
            return "null";
        }
        return raw.length() <= 500 ? raw : raw.substring(0, 500) + "...";
    }
}
