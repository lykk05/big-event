package com.itheima.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiRequest { // 这个用来接收前端传递的参数

    @NotNull(message = "操作类型不能为空")
    private AiAction action; // 操作类型

    private Long articleId; // 文章id

    @NotNull(message = "处理范围不能为空")
    private AiTarget target; // 处理范围 FULL_CONTENT | SELECTION

    @Size(max = 100, message = "标题不能超过100个字符")
    private String title; // 文章标题

    private Integer categoryId; // 分类id

    @NotBlank(message = "文章内容不能为空")
    @Size(max = 20000, message = "文章内容不能超过20000个字符")
    private String content; // 文章内容

    @Size(max = 5000, message = "选中内容不能超过5000个字符")
    private String selectedText; // 选中内容

    @Size(max = 500, message = "自定义要求不能超过500个字符")
    private String instruction; // AI请求信息 CUSTOM时必填
}

