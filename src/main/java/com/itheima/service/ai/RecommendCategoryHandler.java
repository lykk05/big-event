package com.itheima.service.ai;

import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;
import com.itheima.pojo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecommendCategoryHandler extends AbstractAiActionHandler {

    @Override
    public AiAction action() {
        return AiAction.RECOMMEND_CATEGORY;
    }

    @Override
    public String systemPrompt(AiRequest request) {
        return "你是一个文章分类推荐助手。分析用户提供的文章内容或标题，从以下分类中选择最合适的一个：1-美食、2-娱乐、3-军事、4-旅游、5-科技、6-历史。" +
                "你必须以严格的JSON对象格式返回，包含id(数字)、name(分类名称)、reason(推荐理由)。" +
                "格式示例：{\"id\":4,\"name\":\"旅游\",\"reason\":\"文章主要介绍旅游相关内容\"}";
    }

    @Override
    public String userContent(AiRequest request) {
        StringBuilder sb = new StringBuilder();
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            sb.append("文章标题：").append(request.getTitle()).append("\n");
        }
        sb.append("文章内容摘要：\n").append(stripHtmlTags(request.getContent()));
        return sb.toString();
    }

    @Override
    public AiOutputVO parse(String rawResponse) {
        AiOutputVO output = new AiOutputVO();
        output.setContentType("HTML");
        CategoryVO category = parseJsonObj(cleanAiResponse(rawResponse), CategoryVO.class);
        if (category == null) {
            log.warn("AI 未返回有效分类，raw={}", abbreviate(rawResponse));
        }
        output.setCategory(category);
        return output;
    }
}
