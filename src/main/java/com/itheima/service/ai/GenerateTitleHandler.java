package com.itheima.service.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GenerateTitleHandler extends AbstractAiActionHandler {

    @Override
    public AiAction action() {
        return AiAction.GENERATE_TITLE;
    }

    @Override
    public String systemPrompt(AiRequest request) {
        return "你是一个专业的文章标题生成助手。根据用户提供的文章内容，生成5个吸引人、简洁、符合中文阅读习惯的文章标题。" +
                "你必须以严格的JSON数组格式返回，不要包含任何其他文字。格式示例：[\"标题1\",\"标题2\",\"标题3\",\"标题4\",\"标题5\"]";
    }

    @Override
    public String userContent(AiRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("文章内容：\n").append(stripHtmlTags(request.getContent()));
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            sb.append("\n原标题参考：").append(request.getTitle());
        }
        return sb.toString();
    }

    @Override
    public AiOutputVO parse(String rawResponse) {
        String cleaned = cleanAiResponse(rawResponse);
        AiOutputVO output = new AiOutputVO();
        output.setContentType("HTML");
        List<String> titles = parseJsonList(cleaned, new TypeReference<List<String>>() {});
        output.setTitles(titles != null ? titles : extractTitlesFromText(cleaned));
        return output;
    }
}
