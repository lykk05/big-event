package com.itheima.pojo;

import lombok.Data;
import java.util.List;

@Data
public class AiOutputVO {
    private String content; // AI返回的文章内容
    private String contentType; // 文章类型
    private List<String> titles; // 文章标题
    private CategoryVO category; // 文章分类
    private List<CorrectionVO> corrections; // 校对文章
}
