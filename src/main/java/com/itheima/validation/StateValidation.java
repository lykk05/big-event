package com.itheima.validation;

import com.itheima.anno.State;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// State --> 给哪个注解提供校验规则 String --> 校验的数据类型
public class StateValidation implements ConstraintValidator<State, String> {

    // value --> 需要校验的值  context --> 上下文
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 提供校验的规则 --> 返回false 表示校验失败 --> 返回true 表示校验成功
        if (value == null){
            return false;
        }
        if ("已发布".equals(value) || "草稿".equals(value)){
            return true;
        }
        return false;
    }
}
