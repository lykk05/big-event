package com.itheima.controller;

import com.itheima.pojo.AiRequest;
import com.itheima.pojo.Result;
import com.itheima.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.itheima.pojo.AiResultVO;

@RestController
@RequestMapping("/ai/writing")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/generate")
    public Result<AiResultVO> generate(@Valid @RequestBody AiRequest request) {
        return Result.success(aiService.generate(request));
    }
}
