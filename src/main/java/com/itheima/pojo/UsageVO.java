package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageVO {
    private String model; // 模型名称
    private Integer inputTokens; // 输入token数
    private Integer outputTokens; // 输出token数
    private Long latencyMs; // 响应延迟时间（毫秒）
}
