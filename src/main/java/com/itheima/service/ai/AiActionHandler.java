package com.itheima.service.ai;

import com.itheima.pojo.AiAction;
import com.itheima.pojo.AiOutputVO;
import com.itheima.pojo.AiRequest;

/**
 * 一个实现类承载一种 AI 动作的完整契约：提示词、用户内容、响应解析。
 */
public interface AiActionHandler {

    AiAction action();

    String systemPrompt(AiRequest request);

    String userContent(AiRequest request);

    AiOutputVO parse(String rawResponse) throws Exception;
}
