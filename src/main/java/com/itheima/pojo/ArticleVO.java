package com.itheima.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章出参：不暴露 createUser 等内部字段。
 */
@Data
public class ArticleVO {

    private Integer id;
    private String title;
    private String content;
    private String coverImg;
    private String state;
    private Integer categoryId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
