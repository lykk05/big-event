package com.itheima.service.ai;

import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;
import org.springframework.stereotype.Component;

@Component
public class ContinueHandler extends AbstractAiActionHandler {

    @Override
    public AiAction action() {
        return AiAction.CONTINUE;
    }

    @Override
    public String systemPrompt(AiRequest request) {
        return "你是一个专业的写作续写助手。基于用户提供的文章内容和上下文，继续撰写后续段落。" +
                "风格、语气和格式要与原文保持一致。返回续写部分的HTML格式内容（不含原文）。";
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
