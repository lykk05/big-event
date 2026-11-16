package com.itheima.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;
import com.itheima.pojo.AiTarget;
import com.itheima.pojo.CorrectionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ProofreadHandler extends AbstractAiActionHandler {

    @Override
    public AiAction action() {
        return AiAction.PROOFREAD;
    }

    @Override
    public String systemPrompt(AiRequest request) {
        return "你是一个专业的中文校对助手。检查用户提供的文本中的错别字、语法错误、标点问题等。" +
                "你必须以严格的JSON格式返回，包含content(修正后的完整HTML文本)和corrections(纠错列表)。" +
                "corrections每个元素格式：{\"original\":\"原文错误\",\"replacement\":\"修正后\",\"reason\":\"原因说明\"}";
    }

    @Override
    public String userContent(AiRequest request) {
        if (request.getTarget() == AiTarget.SELECTION) {
            return "需要检查的选中内容：\n" + request.getSelectedText();
        }
        return "需要检查的全文：\n" + request.getContent();
    }

    @Override
    @SuppressWarnings("unchecked")
    public AiOutputVO parse(String rawResponse) {
        String cleaned = cleanAiResponse(rawResponse);
        AiOutputVO output = new AiOutputVO();
        output.setContentType("HTML");
        try {
            Map<String, Object> proofMap = OBJECT_MAPPER.readValue(cleaned, new TypeReference<Map<String, Object>>() {});
            output.setContent((String) proofMap.get("content"));
            List<Map<String, String>> correctionList = (List<Map<String, String>>) proofMap.get("corrections");
            if (correctionList != null) {
                output.setCorrections(correctionList.stream()
                        .map(m -> new CorrectionVO(m.get("original"), m.get("replacement"), m.get("reason")))
                        .toList());
            }
        } catch (Exception e) {
            log.warn("AI 校对结果解析失败，raw={}", abbreviate(rawResponse), e);
            output.setContent(cleaned);
            output.setCorrections(List.of());
        }
        return output;
    }
}
