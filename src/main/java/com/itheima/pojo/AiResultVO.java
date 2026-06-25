package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiResultVO {
    private String action; // 操作类型
    private AiOutputVO output; // AI返回的文章内容
    private UsageVO usage; // 模型使用情况
}
