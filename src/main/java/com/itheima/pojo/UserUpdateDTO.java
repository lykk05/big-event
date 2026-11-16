package com.itheima.pojo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户资料更新入参：只暴露允许修改的字段，id 由服务端从登录上下文获取。
 */
@Data
public class UserUpdateDTO {

    @NotEmpty(message = "昵称不能为空")
    @Pattern(regexp = "^\\S{1,10}$", message = "昵称长度为1-10位，不能包含空格")
    private String nickname;

    @NotEmpty(message = "邮箱不能为空")
    @Email
    private String email;
}
