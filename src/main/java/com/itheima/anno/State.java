package com.itheima.anno;

import com.itheima.validation.StateValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented // 元注解 --> 表示该注解被javadoc工具所生成文档
@Target({ElementType.FIELD}) // 表示当前注解所修饰的元素类型
@Retention(RetentionPolicy.RUNTIME) // 表示当前注解所存活的时间
@Constraint(validatedBy = { StateValidation.class }) // 表示当前注解需要关联的校验器类
public @interface State {
    // 错误提示信息
    String message() default "state参数的值只能是已发布或草稿";

    // 分组
    Class<?>[] groups() default {};

    // 负载
    Class<? extends Payload>[] payload() default {};
}
