package com.itheima.service.ai;

import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomHandler extends AbstractAiActionHandler {

    @Override
    public AiAction action() {
        return AiAction.CUSTOM;
    }

    @Override
    public String systemPrompt(AiRequest request) {
        return "你是一个全能的写作助手。严格按照用户的自定义指令要求处理文章内容。";
    }

    @Override
    public String userContent(AiRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("用户指令：").append(request.getInstruction()).append("\n");
        sb.append("文章标题：").append(request.getTitle() != null ? request.getTitle() : "未命名").append("\n");
        sb.append("文章内容：\n").append(request.getContent());
        return sb.toString();
    }

    @Override
    public AiOutputVO parse(String rawResponse) {
        return contentOutput(rawResponse);
    }
}
