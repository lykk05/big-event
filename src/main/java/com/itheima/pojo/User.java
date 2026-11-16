package com.itheima.pojo;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

// lombok --> 在编译阶段为实体类自动生成getter/setter/toString方法
@Data
public class User {
    private Integer id;//主键ID

    private String username;//用户名

    @JsonIgnore // --> 让springMVC把当前对象转换为json的时候忽略该字段
    private String password;//密码

    @NotEmpty(message = "昵称不能为空")
    @Pattern(regexp = "^\\S{1,10}$", message = "昵称长度为1-10位，不能包含空格")
    private String nickname;//昵称

    @NotEmpty(message = "邮箱不能为空")
    @Email
    private String email;//邮箱
    private String userPic;//用户头像地址
    private LocalDateTime createTime;//创建时间
    private LocalDateTime updateTime;//更新时间
}
