package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CorrectionVO {
    private String original; // 原始内容
    private String replacement; // 替换内容
    private String reason; // 校对原因
}
