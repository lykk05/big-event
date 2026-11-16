package com.itheima.service.ai;

import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;
import org.springframework.stereotype.Component;

@Component
public class ExpandHandler extends AbstractAiActionHandler {

    @Override
    public AiAction action() {
        return AiAction.EXPAND;
    }

    @Override
    public String systemPrompt(AiRequest request) {
        return "你是一个专业的文本扩写助手。将用户提供的内容进行详细扩展和丰富，增加细节描述、背景说明或案例分析。" +
                "保持原有结构和核心观点不变。返回扩写后的完整HTML格式内容。";
    }

    @Override
    public String userContent(AiRequest request) {
        return "需要扩写的内容：\n" + request.getSelectedText()
                + "\n\n文章上下文：\n" + stripHtmlTags(request.getContent());
    }

    @Override
    public AiOutputVO parse(String rawResponse) {
        return contentOutput(rawResponse);
    }
}
