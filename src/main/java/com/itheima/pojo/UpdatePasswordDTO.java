package com.itheima.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 修改密码入参：兼容前端现有的下划线字段名。
 */
@Data
public class UpdatePasswordDTO {

    @NotBlank(message = "原密码不能为空")
    @JsonProperty("old_pwd")
    private String oldPwd;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^\\S{5,16}$", message = "新密码长度为5-16位，不能包含空格")
    @JsonProperty("new_pwd")
    private String newPwd;

    @NotBlank(message = "确认密码不能为空")
    @JsonProperty("re_pwd")
    private String rePwd;
}
