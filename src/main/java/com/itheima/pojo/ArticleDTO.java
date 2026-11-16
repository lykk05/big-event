package com.itheima.pojo;

import com.itheima.anno.State;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * 文章新增/更新入参：只暴露允许客户端提交的字段。
 */
@Data
public class ArticleDTO {

    private Integer id; // 更新时必填，新增时忽略

    @NotEmpty(message = "标题不能为空")
    @Pattern(regexp = "^\\S{1,10}$", message = "标题长度为1-10位，不能包含空格")
    private String title;

    @NotEmpty(message = "文章内容不能为空")
    @Size(max = 10000, message = "文章内容不能超过10000个字符")
    private String content;

    @NotEmpty(message = "封面不能为空")
    @URL(message = "封面地址格式不正确")
    @Size(max = 128, message = "封面地址不能超过128个字符")
    private String coverImg;

    @State
    private String state;

    @NotNull(message = "分类不能为空")
    private Integer categoryId;
}
