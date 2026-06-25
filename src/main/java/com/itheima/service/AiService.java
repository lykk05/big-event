package com.itheima.service;

import com.itheima.pojo.AiRequest;
import com.itheima.pojo.AiResultVO;

public interface AiService {
    AiResultVO generate(AiRequest request);
}
