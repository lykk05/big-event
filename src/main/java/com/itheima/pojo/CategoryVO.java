package com.itheima.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO {
    private Long id; // 分类id
    private String name; // 分类名称
    private String reason; // 推荐理由
}