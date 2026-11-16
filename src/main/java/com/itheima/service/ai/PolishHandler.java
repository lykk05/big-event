package com.itheima.service.ai;

import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;
import org.springframework.stereotype.Component;

@Component
public class PolishHandler extends AbstractAiActionHandler {

    @Override
    public AiAction action() {
        return AiAction.POLISH;
    }

    @Override
    public String systemPrompt(AiRequest request) {
        return "你是一个专业的中文文本润色助手。对用户提供的文章进行润色优化，使语言更自然流畅、表达更精准有力。" +
                "保持原文的事实和核心信息不变，只优化语言表达。返回润色后的完整HTML格式内容。";
    }

    @Override
    public String userContent(AiRequest request) {
        return "文章标题：" + (request.getTitle() != null ? request.getTitle() : "未命名")
                + "\n文章内容：\n" + request.getContent();
    }

    @Override
    public AiOutputVO parse(String rawResponse) {
        return contentOutput(rawResponse);
    }
}
