package com.itheima.controller;

import com.itheima.pojo.AiRequest;
import com.itheima.pojo.AiResultVO;
import com.itheima.pojo.Result;
import com.itheima.service.AiService;
import com.itheima.utils.ThreadLocalUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai/writing")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/generate")
    public Result<AiResultVO> generate(@Valid @RequestBody AiRequest request) {
        Map<String, Object> claims = ThreadLocalUtil.get();
        Integer userId = (Integer) claims.get("id");
        return Result.success(aiService.generate(request, userId));
    }
}
